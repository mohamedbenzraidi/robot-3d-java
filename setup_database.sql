-- ========================================
-- 3D Museum Tour - Database Setup Script
-- ========================================
-- Run this script to create the Paintings table and insert sample data

-- Drop existing table if it exists
DROP TABLE IF EXISTS Paintings CASCADE;

-- Create Paintings table
CREATE TABLE Paintings (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    year INTEGER,
    description TEXT
);

-- Insert Louvre Museum paintings (painting1-painting15)
INSERT INTO Paintings (id, title, artist, year, description) VALUES
('painting1', 'Mona Lisa', 'Leonardo da Vinci', 1503, 'The Mona Lisa is a half-length portrait painting by Italian artist Leonardo da Vinci. Considered an archetypal masterpiece of the Italian Renaissance, it has been described as "the best known, the most visited, the most written about, the most sung about, the most parodied work of art in the world".'),
('painting2', 'Liberty Leading the People', 'Eugène Delacroix', 1830, 'A painting commemorating the July Revolution of 1830, which toppled King Charles X. A woman personifying Liberty leads the people forward over a barricade and the bodies of the fallen, holding the flag of the French Revolution.'),
('painting3', 'The Wedding at Cana', 'Paolo Veronese', 1563, 'This monumental canvas depicts the biblical story of the Wedding at Cana, at which Jesus miraculously converts water into wine. It is the largest painting in the Louvre Museum.'),
('painting4', 'The Coronation of Napoleon', 'Jacques-Louis David', 1807, 'This painting depicts the coronation of Napoleon I at Notre-Dame de Paris in 1804. The Emperor crowns himself while the Pope sits behind him.'),
('painting5', 'The Raft of the Medusa', 'Théodore Géricault', 1819, 'An iconic work of French Romanticism, this painting depicts the aftermath of the wreck of the French naval frigate Méduse. The survivors are shown on a makeshift raft drifting at sea.'),
('painting6', 'The Lacemaker', 'Johannes Vermeer', 1669, 'A small painting depicting a young woman in a yellow shawl, concentrating intently on her lacework. It is one of Vermeer''s most famous works.'),
('painting7', 'Grande Odalisque', 'Jean-Auguste-Dominique Ingres', 1814, 'An oil painting of a nude reclining woman. Though Ingres'' work was criticized for anatomical distortions, it remains one of his most famous paintings.'),
('painting8', 'The Turkish Bath', 'Jean-Auguste-Dominique Ingres', 1862, 'Completed when Ingres was 82 years old, this circular painting depicts a group of nude women in a harem. It represents the culmination of his fascination with Orientalist themes.'),
('painting9', 'Psyche Revived by Cupid''s Kiss', 'Antonio Canova', 1793, 'A neoclassical sculpture depicting Cupid awakening the lifeless Psyche with a kiss. The marble figures appear almost weightless, exemplifying Canova''s technical mastery.'),
('painting10', 'The Virgin and Child with Saint Anne', 'Leonardo da Vinci', 1503, 'An oil painting depicting Saint Anne, her daughter the Virgin Mary and the infant Jesus. Christ is shown grappling with a sacrificial lamb symbolizing his Passion.'),
('painting11', 'The Oath of the Horatii', 'Jacques-Louis David', 1785, 'A large painting that depicts a scene from a Roman legend about a dispute between two warring cities, Rome and Alba Longa. It is a masterpiece of Neoclassicism.'),
('painting12', 'Diana Bathing', 'François Boucher', 1742, 'A Rococo painting depicting the goddess Diana and her nymphs after hunting. The sensual treatment of the nude figures is characteristic of Boucher''s style.'),
('painting13', 'The Astronomer', 'Johannes Vermeer', 1668, 'One of only two known Vermeer paintings of male subjects. It depicts a scholar studying a celestial globe, representing the pursuit of scientific knowledge.'),
('painting14', 'Gabrielle d''Estrées and One of Her Sisters', 'Unknown French Artist', 1594, 'An enigmatic portrait of Gabrielle d''Estrées, mistress of King Henry IV of France, with her sister. The unusual gesture of the sister has been the subject of much speculation.'),
('painting15', 'Saint John the Baptist', 'Leonardo da Vinci', 1516, 'Leonardo''s last painting, depicting John the Baptist in a dark setting, pointing heavenward. The mysterious smile echoes that of the Mona Lisa.');

-- Insert Metropolitan Museum paintings (painting16-painting30)
INSERT INTO Paintings (id, title, artist, year, description) VALUES
('painting16', 'Washington Crossing the Delaware', 'Emanuel Leutze', 1851, 'This iconic painting depicts George Washington''s crossing of the Delaware River on December 25, 1776, during the American Revolutionary War. It has become an iconic image symbolizing American patriotism and determination.'),
('painting17', 'Madame X', 'John Singer Sargent', 1884, 'A portrait of Virginie Amélie Avegno Gautreau, a Parisian socialite. The painting caused a scandal when first exhibited due to the subject''s revealing dress and provocative pose, but is now considered one of Sargent''s finest works.'),
('painting18', 'The Death of Socrates', 'Jacques-Louis David', 1787, 'A neoclassical painting depicting the Greek philosopher Socrates moments before his death by poisoning, as described in Plato''s Phaedo. It exemplifies the stoic philosophy of accepting death with dignity.'),
('painting19', 'Autumn Rhythm (Number 30)', 'Jackson Pollock', 1950, 'A large abstract expressionist painting created using Pollock''s drip painting technique. The work exemplifies the spontaneous, physical method that made Pollock famous.'),
('painting20', 'The Harvesters', 'Pieter Bruegel the Elder', 1565, 'Part of a series depicting different times of the year, this painting shows peasants harvesting wheat. It is one of only about forty surviving Bruegel paintings.'),
('painting21', 'Self-Portrait with a Straw Hat', 'Vincent van Gogh', 1887, 'Painted during van Gogh''s Paris period, this self-portrait shows the artist wearing a straw hat. The painting demonstrates his developing post-impressionist style and bold use of color.'),
('painting22', 'Bridge over a Pond of Water Lilies', 'Claude Monet', 1899, 'One of Monet''s famous paintings of the Japanese bridge in his garden at Giverny. The work captures the interplay of light, water, and vegetation that fascinated the artist throughout his career.'),
('painting23', 'Madonna and Child', 'Duccio di Buoninsegna', 1300, 'A small but exquisite panel painting from the Italian Proto-Renaissance. The golden background and tender relationship between mother and child are characteristic of Duccio''s style.'),
('painting24', 'Aristotle with a Bust of Homer', 'Rembrandt van Rijn', 1653, 'A masterpiece depicting the ancient Greek philosopher Aristotle contemplating a bust of the poet Homer. The painting explores themes of mortality, achievement, and the passage of time.'),
('painting25', 'Young Mother Sewing', 'Mary Cassatt', 1900, 'An American Impressionist painting depicting an intimate domestic scene. Cassatt specialized in portraying the lives of women, particularly the bonds between mothers and children.'),
('painting26', 'The Musicians', 'Caravaggio', 1597, 'An early work by Caravaggio showing four boys practicing music. The painting demonstrates the artist''s developing use of dramatic lighting and realistic depiction of subjects.'),
('painting27', 'Joan of Arc', 'Jules Bastien-Lepage', 1879, 'A large painting depicting Joan of Arc in her parents'' garden, experiencing a divine vision. The work combines realism with symbolism in its portrayal of the French heroine.'),
('painting28', 'The Gulf Stream', 'Winslow Homer', 1899, 'A powerful painting showing a man in a small dismasted boat surrounded by sharks. It is considered one of Homer''s masterpieces and explores themes of man versus nature.'),
('painting29', 'The Dance Class', 'Edgar Degas', 1874, 'One of Degas'' famous ballet scenes, depicting young dancers during a rehearsal. The painting showcases Degas'' skill in capturing movement and his innovative compositions.'),
('painting30', 'Cypresses', 'Vincent van Gogh', 1889, 'Painted during van Gogh''s stay at the asylum in Saint-Rémy, this work features the cypress trees that fascinated him. The swirling brushwork and intense colors are characteristic of his late style.');

-- Verify data was inserted
SELECT COUNT(*) as total_paintings FROM Paintings;

-- Show sample data
SELECT id, title, artist, year FROM Paintings ORDER BY id LIMIT 10;

COMMIT;

