package org.example.DB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBMain {
    public static void main(String[] args) throws SQLException {
        DBManager db = new DBManager();

        db.dropTable("Paintings");

        String query = """
            CREATE TABLE IF NOT EXISTS Paintings(
                id VARCHAR(50) PRIMARY KEY,
                title VARCHAR(150),
                artist VARCHAR(180),
                year INTEGER,
                description VARCHAR(255)
            );""";

        db.createTable(query);

        List<String[]> data = new ArrayList<>();

        String[] ids = new String[49];
        for(int i=0; i<49; i++){
            ids[i] = "painting"+(i+1);
        }

        String[] titles = {
                "The Starry Night",
                "View of Toledo",
                "The Son of Man",
                "Girl with a Pearl Earring",
                "Mona Lisa",
                "Napoleon Crossing the Alps",
                "Las Meninas",
                "Woman with a Parasol",
                "Woman I",
                "The Bohemian",
                "The Kiss",
                "The Scream",
                "Guernica",
                "The Night Watch",
                "American Gothic",
                "The Persistence of Memory",
                "Impression, Sunrise",
                "A Sunday Afternoon on the Island of La Grande Jatte",
                "Whistler's Mother",
                "The Great Wave off Kanagawa",
                "Wanderer above the Sea of Fog",
                "Nighthawks",
                "The Arnolfini Portrait",
                "Composition 8",
                "Cafe Terrace at Night",
                "No. 5, 1948",
                "Sunflowers",
                "Water Lilies and Japanese Bridge",
                "The Tower of Babel",
                "The Fighting Temeraire",
                "The Milkmaid",
                "The Old Guitarist",
                "Christina's World",
                "Broadway Boogie Woogie",
                "The Blue Boy",
                "The Banjo Lesson",
                "The Bedroom",
                "The Potato Eaters",
                "The Card Players",
                "Golconda",
                "The Gleaners",
                "Breezing Up (A Fair Wind)",
                "I and the Village",
                "Portrait of Adele Bloch-Bauer I",
                "Drowning Girl",
                "The Flower Carrier",
                "The Desperate Man",
                "The Angelus",
                "Paris Street; Rainy Day"
        };

        String[] artists = {
                "Vincent van Gogh",
                "El Greco",
                "René Magritte",
                "Johannes Vermeer",
                "Leonardo da Vinci",
                "Jacques-Louis David",
                "Diego Velázquez",
                "Claude Monet",
                "Willem de Kooning",
                "William-Adolphe Bouguereau",
                "Gustav Klimt",
                "Edvard Munch",
                "Pablo Picasso",
                "Rembrandt van Rijn",
                "Grant Wood",
                "Salvador Dalí",
                "Claude Monet",
                "Georges Seurat",
                "James McNeill Whistler",
                "Hokusai",
                "Caspar David Friedrich",
                "Edward Hopper",
                "Jan van Eyck",
                "Wassily Kandinsky",
                "Vincent van Gogh",
                "Jackson Pollock",
                "Vincent van Gogh",
                "Claude Monet",
                "Pieter Bruegel the Elder",
                "J.M.W. Turner",
                "Johannes Vermeer",
                "Pablo Picasso",
                "Andrew Wyeth",
                "Piet Mondrian",
                "Thomas Gainsborough",
                "Henry Ossawa Tanner",
                "Vincent van Gogh",
                "Vincent van Gogh",
                "Paul Cézanne",
                "René Magritte",
                "Jean-François Millet",
                "Winslow Homer",
                "Marc Chagall",
                "Gustav Klimt",
                "Roy Lichtenstein",
                "Diego Rivera",
                "Gustave Courbet",
                "Jean-François Millet",
                "Gustave Caillebotte"
        };

        String[] years = {
                "1889",
                "1600",
                "1964",
                "1665",
                "1503",
                "1801",
                "1656",
                "1875",
                "1950",
                "1890",
                "1907",
                "1893",
                "1937",
                "1642",
                "1930",
                "1931",
                "1872",
                "1884",
                "1871",
                "1831",
                "1818",
                "1942",
                "1434",
                "1923",
                "1888",
                "1948",
                "1888",
                "1899",
                "1563",
                "1839",
                "1658",
                "1903",
                "1948",
                "1943",
                "1770",
                "1893",
                "1888",
                "1885",
                "1895",
                "1953",
                "1857",
                "1876",
                "1911",
                "1907",
                "1963",
                "1935",
                "1845",
                "1859",
                "1877"
        };

        String[] descriptions = {
                "Vincent van Gogh's 1889 masterpiece, The Starry Night, features a swirling night sky over a sleeping village.",
                "El Greco's View of Toledo (c. 1600) features a dramatic, stormy sky over the Spanish city.",
                "The Son of Man by René Magritte (1964) is a surrealist work showing a man with a green apple hovering in front of his face.",
                "Johannes Vermeer's Girl with a Pearl Earring (1665) is a 'tronie' depicting a young girl wearing an exotic turban and a large pearl.",
                "Leonardo da Vinci's Mona Lisa (1503) is famous for the subject's enigmatic expression and subtle smile.",
                "Jacques-Louis David's Napoleon Crossing the Alps (1801) depicts the leader guiding his army through the mountains.",
                "Diego Velázquez's Las Meninas (1656) is a complex portrait of the Spanish royal court that includes the artist himself.",
                "Claude Monet's Woman with a Parasol (1875) depicts the artist's wife and son strolling on a windy summer day.",
                "Willem de Kooning's Woman I (1950) is an abstract expressionist work featuring aggressive brushwork and distorted forms.",
                "William-Adolphe Bouguereau's The Bohemian (1890) realistically depicts a young woman sitting with a violin on her lap.",
                "Gustav Klimt's The Kiss (1907) uses gold leaf to depict a couple embraced in a field of flowers.",
                "Edvard Munch's The Scream (1893) symbolizes modern anxiety through an agonizing, expressionist figure.",
                "Pablo Picasso's Guernica (1937) is an anti-war mural depicting the suffering of civilians during the Spanish Civil War.",
                "Rembrandt van Rijn's The Night Watch (1642) uses light and shadow to depict a city militia moving out.",
                "Grant Wood's American Gothic (1930) shows a stern farmer and his daughter standing before a farmhouse.",
                "Salvador Dalí's The Persistence of Memory (1931) is famous for its melting clocks set in a dreamlike landscape.",
                "Claude Monet's Impression, Sunrise (1872) is a hazy harbor scene that gave the Impressionist movement its name.",
                "Georges Seurat's A Sunday Afternoon on the Island of La Grande Jatte (1884) is a pointillist masterpiece made of millions of tiny dots.",
                "James McNeill Whistler's Whistler's Mother (1871) is an austere profile portrait known as a study in black and grey composition.",
                "Hokusai's The Great Wave off Kanagawa (1831) is a woodblock print showing a massive wave threatening boats near Mount Fuji.",
                "Caspar David Friedrich's Wanderer above the Sea of Fog (1818) shows a figure standing on a precipice overlooking the mist.",
                "Edward Hopper's Nighthawks (1942) depicts people in a late-night diner seen through a window.",
                "Jan van Eyck's The Arnolfini Portrait (1434) is famous for its detailed symbolism and convex mirror.",
                "Wassily Kandinsky's Composition 8 (1923) explores the abstract relationship between geometric forms and color.",
                "Vincent van Gogh's Cafe Terrace at Night (1888) captures a colorful cafe in Arles under a starry sky.",
                "Jackson Pollock's No. 5, 1948 features the artist's signature chaotic drips and splatters of paint.",
                "Vincent van Gogh's Sunflowers (1888) is a vibrant still life featuring yellow flowers that symbolize gratitude.",
                "Claude Monet's Water Lilies and Japanese Bridge (1899) depicts the serene footbridge over the pond in the artist's garden.",
                "Pieter Bruegel the Elder's The Tower of Babel (1563) illustrates the biblical story of a tower built to reach heaven.",
                "J.M.W. Turner's The Fighting Temeraire (1839) shows an old warship being towed down the river by a steam tug.",
                "Johannes Vermeer's The Milkmaid (c. 1658) depicts a kitchen maid pouring milk with quiet concentration.",
                "Pablo Picasso's The Old Guitarist (1903) depicts a frail, blind musician during the artist's Blue Period.",
                "Andrew Wyeth's Christina's World (1948) shows a young woman in a field looking toward a distant farmhouse.",
                "Piet Mondrian's Broadway Boogie Woogie (1943) uses an abstract grid to represent the rhythm and energy of New York City.",
                "Thomas Gainsborough's The Blue Boy (1770) features a boy dressed in an elaborate blue satin costume.",
                "Henry Ossawa Tanner's The Banjo Lesson (1893) depicts an elderly man teaching a young boy to play the banjo.",
                "Vincent van Gogh's The Bedroom (1888) uses bold colors to depict the artist's simple room in Arles.",
                "Vincent van Gogh's The Potato Eaters (1885) is a dark work depicting a peasant family gathered for a humble meal.",
                "Paul Cézanne's The Card Players (c. 1895) shows two peasants focused on a card game.",
                "René Magritte's Golconda (1953) is a surreal image featuring men in bowler hats falling like rain.",
                "Jean-François Millet's The Gleaners (1857) depicts three women gathering leftover grain after the harvest.",
                "Winslow Homer's Breezing Up (A Fair Wind) (1876) shows a father and three boys sailing on a choppy sea.",
                "Marc Chagall's I and the Village (1911) is a dreamlike work recalling the artist's memories of Russian folklore.",
                "Gustav Klimt's Portrait of Adele Bloch-Bauer I (1907) is a golden portrait famous for its intricate ornamentation.",
                "Roy Lichtenstein's Drowning Girl (1963) uses comic book aesthetics to depict a dramatic scene.",
                "Diego Rivera's The Flower Carrier (1935) shows a peasant struggling to stand under a giant basket of flowers.",
                "Gustave Courbet's The Desperate Man (1845) is an intense self-portrait capturing the artist staring wildly at the viewer.",
                "Jean-François Millet's The Angelus (1859) shows two farmers praying in a field at twilight.",
                "Gustave Caillebotte's Paris Street; Rainy Day (1877) captures a large, rainy intersection in Paris."
        };

        data.add(ids);
        data.add(titles);
        data.add(artists);
        data.add(years);
        data.add(descriptions);

        int rowCount = 0;
        ResultSet rs;
        rs = db.readQuery("SELECT COUNT(*) AS rowCount FROM Paintings;");
        if(rs.next()) rowCount = rs.getInt("rowCount");

        if(rowCount == 0){
            query = "INSERT INTO Paintings (id, title, artist, year, description) VALUES (?,?,?,?,?);";
            db.bulkInsert(query, data);
        }

        rs = db.readQuery("SELECT * FROM Paintings;");

        while(rs.next()){
            System.out.println("id : " + rs.getString(1));
            System.out.println("title : " + rs.getString(2));
            System.out.println("artist : " + rs.getString(3));
            System.out.println("year : " + rs.getInt(4));
            System.out.println("description : " + rs.getString(5));
            System.out.println("==============================================");
        }
        db.closeConnection();


    }
}
