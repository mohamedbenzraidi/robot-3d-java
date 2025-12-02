# 🔧 Database Connection Issue - Fix Guide

## 🎯 Problem Summary

When clicking on a painting (tableau), the application crashes. This happens due to **TWO issues**:

### 1. **Texture Loading Crash (Primary Cause)**
- The JVM crashes when loading JPEG textures
- Error in native graphics driver: `igxelpicd64.dll` (Intel Graphics)
- Happens during: `glTexImage2D` (OpenGL texture upload)

### 2. **Database Connection Issues (Secondary Cause)**
- If Docker PostgreSQL container is not running
- Database queries return `null` → causes NullPointerException
- Application tries to display null text in UI

---

## ✅ Solution Steps

### Step 1: Start Docker PostgreSQL Container

```bash
# Check if container exists
docker ps -a

# If container exists but stopped, START it:
docker start my-postgres-container

# If container doesn't exist, CREATE it:
docker run --name my-postgres-container \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=tour_3d_db \
  -p 5432:5432 \
  -d postgres

# Verify it's running
docker ps
```

**Expected output:**
```
CONTAINER ID   IMAGE      STATUS         PORTS                    NAMES
abc123def456   postgres   Up 2 seconds   0.0.0.0:5432->5432/tcp   my-postgres-container
```

---

### Step 2: Verify Database Connection

```bash
# Check container logs
docker logs my-postgres-container

# Connect to database (to test)
docker exec -it my-postgres-container psql -U user -d tour_3d_db
```

Inside PostgreSQL shell:
```sql
-- List tables
\dt

-- Check if Paintings table exists
SELECT * FROM Paintings LIMIT 5;

-- Exit
\q
```

---

### Step 3: Create Database Table (If Needed)

If the `Paintings` table doesn't exist, create it:

```sql
CREATE TABLE Paintings (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    year INTEGER,
    description TEXT
);

-- Insert sample data
INSERT INTO Paintings (id, title, artist, year, description) VALUES
('painting1', 'Mona Lisa', 'Leonardo da Vinci', 1503, 'The most famous painting in the world'),
('painting2', 'Starry Night', 'Vincent van Gogh', 1889, 'A swirling night sky over a French village'),
('painting3', 'The Scream', 'Edvard Munch', 1893, 'An iconic image of anxiety and existential dread'),
('painting4', 'The Persistence of Memory', 'Salvador Dalí', 1931, 'Melting clocks in a surreal landscape'),
('painting5', 'The Last Supper', 'Leonardo da Vinci', 1498, 'Jesus final meal with his apostles');
-- ... add more paintings (painting6-painting30)
```

---

### Step 4: Fix Texture Issues (Optional)

If the app still crashes after database is running, the issue is **texture loading**.

**Possible fixes:**

1. **Reduce texture sizes:**
   ```bash
   # Use ImageMagick or similar tool to resize JPEGs
   # In Textures/ folder
   for file in *.jpg; do
       convert "$file" -resize 1024x1024 -quality 85 "$file"
   done
   ```

2. **Convert JPEGs to PNG:**
   ```bash
   for file in *.jpg; do
       convert "$file" "${file%.jpg}.png"
   done
   ```

3. **Update Java/Graphics Drivers:**
   - Update Intel Graphics drivers
   - Try running with different Java version
   - Add JVM argument: `-Dsun.java2d.d3d=false`

---

## 🐛 Troubleshooting

### Issue: "Connection refused"

```bash
# Check if port 5432 is in use
netstat -an | findstr 5432   # Windows
lsof -i :5432                # Mac/Linux

# If another service uses port 5432, use a different port:
docker run --name my-postgres-container \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=tour_3d_db \
  -p 5433:5432 \
  -d postgres

# Then update DBConnection.java:
# private static final String url = "jdbc:postgresql://localhost:5433/tour_3d_db";
```

---

### Issue: "Container already exists"

```bash
# Remove old container
docker rm my-postgres-container

# Then recreate it (see Step 1)
```

---

### Issue: App still crashes

**Check the console output:**

1. Look for: `"✅ SUCCESS! Connected to database"`
   - If YES → Database is working, issue is textures
   - If NO → Database connection failed

2. Look for: `"❌ DATABASE ERROR"`
   - Follow troubleshooting steps above

3. Look for: `"EXCEPTION_ACCESS_VIOLATION"`
   - This is a native crash (graphics driver)
   - Try the texture fixes in Step 4

---

## 🔍 What Changed in the Code

I've added **better error handling** so the app won't crash:

### ✅ Changes Made:

1. **SceneManager.java** & **MetSceneManager.java**:
   - Added `try-catch` in `detectPaintingClick()`
   - Added null checks for database results
   - Shows error message instead of crashing

2. **DBConnection.java**:
   - Added retry mechanism (3 attempts)
   - Better error messages with troubleshooting steps
   - Added `testConnection()` method

3. **DBManager.java**:
   - Added null checks before database operations
   - Better logging for debugging
   - Warns if connection is null instead of crashing

---

## 🚀 Quick Start Command

```bash
# All-in-one command to start fresh:
docker rm -f my-postgres-container 2>/dev/null || true && \
docker run --name my-postgres-container \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=tour_3d_db \
  -p 5432:5432 \
  -d postgres && \
echo "✅ Database container started!" && \
sleep 5 && \
docker exec -it my-postgres-container psql -U user -d tour_3d_db -c "SELECT version();"
```

---

## 📝 Summary

**What was the problem?**
- Clicking paintings → database query → null result → crash
- Also: JPEG texture loading crash in graphics driver

**What's fixed now?**
- ✅ App won't crash if database is down
- ✅ Shows error message instead
- ✅ Better logging to identify issues
- ✅ Retry mechanism for database connection

**What to do now?**
1. Start Docker container (Step 1)
2. Run the application
3. Click on a painting
4. If it still crashes, check console logs and try texture fixes

---

## 💡 Pro Tips

- **Keep container running**: Start it once, it stays running
- **Check logs**: `docker logs my-postgres-container` shows database errors
- **Monitor app**: Console shows detailed error messages now
- **Test database**: Run the Quick Start Command to verify everything works

---

Need help? Check the console output - it now shows detailed error messages! 🎉

