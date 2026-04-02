-- Seed questions for each scale in table `scale_question`
-- Run with: mysql -u root -p healthy_db -e "source E:/biyelunwen/cursor_download/healthy_system/sql/seed_scale_questions.sql"

USE healthy_db;

SET @opt4 = '["0 Not at all","1 Several days","2 More than half the days","3 Nearly every day"]';
SET @opt5 = '["0 Never","1 Rarely","2 Sometimes","3 Often","4 Almost always"]';

DELETE FROM scale_question WHERE scale_id IN (SELECT id FROM scale WHERE code IN ('SCL-90','PHQ-9','GAD-7','DASS-21','BDI-II'));

UPDATE scale SET question_count = 10 WHERE code = 'SCL-90';
UPDATE scale SET question_count = 9 WHERE code = 'PHQ-9';
UPDATE scale SET question_count = 7 WHERE code = 'GAD-7';
UPDATE scale SET question_count = 10 WHERE code = 'DASS-21';
UPDATE scale SET question_count = 10 WHERE code = 'BDI-II';

-- SCL-90 sample (10)
INSERT INTO scale_question(scale_id, question_no, content, options, reverse_score, dimension)
VALUES
((SELECT id FROM scale WHERE code='SCL-90'),1,'Feeling nervous or anxious',@opt5,0,'Anxiety'),
((SELECT id FROM scale WHERE code='SCL-90'),2,'Trouble sleeping',@opt5,0,'Somatization'),
((SELECT id FROM scale WHERE code='SCL-90'),3,'Feeling low or sad',@opt5,0,'Depression'),
((SELECT id FROM scale WHERE code='SCL-90'),4,'Difficulty concentrating',@opt5,0,'Obsessive-Compulsive'),
((SELECT id FROM scale WHERE code='SCL-90'),5,'Feeling lonely',@opt5,0,'Interpersonal Sensitivity'),
((SELECT id FROM scale WHERE code='SCL-90'),6,'Loss of interest in activities',@opt5,0,'Depression'),
((SELECT id FROM scale WHERE code='SCL-90'),7,'Irritability',@opt5,0,'Hostility'),
((SELECT id FROM scale WHERE code='SCL-90'),8,'Fear without clear reason',@opt5,0,'Phobic Anxiety'),
((SELECT id FROM scale WHERE code='SCL-90'),9,'Physical discomfort under stress',@opt5,0,'Somatization'),
((SELECT id FROM scale WHERE code='SCL-90'),10,'Feeling hopeless about future',@opt5,0,'Depression');

-- PHQ-9 (9)
INSERT INTO scale_question(scale_id, question_no, content, options, reverse_score, dimension)
VALUES
((SELECT id FROM scale WHERE code='PHQ-9'),1,'Little interest or pleasure in doing things',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),2,'Feeling down, depressed, or hopeless',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),3,'Trouble falling or staying asleep, or sleeping too much',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),4,'Feeling tired or having little energy',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),5,'Poor appetite or overeating',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),6,'Feeling bad about yourself',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),7,'Trouble concentrating on things',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),8,'Moving or speaking slowly, or being fidgety/restless',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='PHQ-9'),9,'Thoughts that you would be better off dead or of hurting yourself',@opt4,0,'Depression');

-- GAD-7 (7)
INSERT INTO scale_question(scale_id, question_no, content, options, reverse_score, dimension)
VALUES
((SELECT id FROM scale WHERE code='GAD-7'),1,'Feeling nervous, anxious, or on edge',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='GAD-7'),2,'Not being able to stop or control worrying',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='GAD-7'),3,'Worrying too much about different things',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='GAD-7'),4,'Trouble relaxing',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='GAD-7'),5,'Being so restless that it is hard to sit still',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='GAD-7'),6,'Becoming easily annoyed or irritable',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='GAD-7'),7,'Feeling afraid as if something awful might happen',@opt4,0,'Anxiety');

-- DASS-21 sample (10)
INSERT INTO scale_question(scale_id, question_no, content, options, reverse_score, dimension)
VALUES
((SELECT id FROM scale WHERE code='DASS-21'),1,'I found it hard to wind down',@opt4,0,'Stress'),
((SELECT id FROM scale WHERE code='DASS-21'),2,'I was aware of dryness of my mouth',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='DASS-21'),3,'I could not seem to experience any positive feeling',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='DASS-21'),4,'I experienced breathing difficulty',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='DASS-21'),5,'I found it difficult to work up initiative',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='DASS-21'),6,'I tended to over-react to situations',@opt4,0,'Stress'),
((SELECT id FROM scale WHERE code='DASS-21'),7,'I experienced trembling',@opt4,0,'Anxiety'),
((SELECT id FROM scale WHERE code='DASS-21'),8,'I felt I was using a lot of nervous energy',@opt4,0,'Stress'),
((SELECT id FROM scale WHERE code='DASS-21'),9,'I felt down-hearted and blue',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='DASS-21'),10,'I found it difficult to relax',@opt4,0,'Stress');

-- BDI-II sample (10)
INSERT INTO scale_question(scale_id, question_no, content, options, reverse_score, dimension)
VALUES
((SELECT id FROM scale WHERE code='BDI-II'),1,'Sadness',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),2,'Pessimism',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),3,'Past failure',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),4,'Loss of pleasure',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),5,'Guilty feelings',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),6,'Punishment feelings',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),7,'Self-dislike',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),8,'Self-criticalness',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),9,'Suicidal thoughts or wishes',@opt4,0,'Depression'),
((SELECT id FROM scale WHERE code='BDI-II'),10,'Crying',@opt4,0,'Depression');
