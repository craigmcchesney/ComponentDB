LOCK TABLES `item_element` WRITE;
/*!40000 ALTER TABLE `item_element` DISABLE KEYS */;
INSERT INTO `item_element` VALUES
(1,NULL,1,NULL,NULL,NULL,'',NULL,1),
(2,NULL,2,NULL,NULL,NULL,'',NULL,2),
(3,'1',1,2,NULL,NULL,NULL,NULL,3),
(4,NULL,3,NULL,NULL,NULL,'',NULL,4),
(5,NULL,4,NULL,NULL,NULL,'',NULL,5),
(6,'1',4,3,NULL,NULL,NULL,NULL,8),
(7,'2',4,3,NULL,NULL,NULL,NULL,7),
(8,'4',4,3,NULL,NULL,NULL,NULL,9),
(9,'3',4,3,NULL,NULL,NULL,NULL,6),
(10,NULL,5,NULL,NULL,NULL,'',NULL,10),
(11,NULL,7,NULL,NULL,NULL,NULL,NULL,18),
(12,NULL,7,10,7,NULL,NULL,NULL,15),
(13,NULL,8,NULL,NULL,NULL,'',NULL,12),
(14,NULL,9,NULL,NULL,NULL,NULL,NULL,11),
(15,NULL,7,6,9,NULL,NULL,NULL,14),
(16,NULL,10,NULL,NULL,NULL,NULL,NULL,19),
(17,NULL,7,9,6,NULL,NULL,NULL,16),
(18,NULL,6,NULL,NULL,NULL,NULL,NULL,13),
(19,NULL,7,8,8,NULL,NULL,NULL,17),
(20,NULL,11,13,9,NULL,NULL,NULL,26),
(21,NULL,11,6,7,NULL,NULL,NULL,23),
(22,NULL,12,NULL,NULL,NULL,'',NULL,24),
(23,NULL,11,5,6,NULL,NULL,NULL,22),
(24,NULL,11,NULL,NULL,NULL,NULL,NULL,20),
(25,NULL,11,12,8,NULL,NULL,NULL,21),
(26,NULL,13,NULL,NULL,NULL,NULL,NULL,25);
/*!40000 ALTER TABLE `item_element` ENABLE KEYS */;
UNLOCK TABLES;
