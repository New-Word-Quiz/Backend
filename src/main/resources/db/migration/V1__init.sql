-- MySQL dump 10.13  Distrib 8.0.42, for Win64 (x86_64)
--
-- Host: localhost    Database: slang_quiz_app
-- ------------------------------------------------------
-- Server version	8.0.42

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `hint`
--

DROP TABLE IF EXISTS `hint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hint` (
  `hint_id` bigint NOT NULL AUTO_INCREMENT,
  `quiz_id` bigint NOT NULL,
  `hint_text` varchar(500) NOT NULL,
  `example_text` varchar(500) NOT NULL,
  PRIMARY KEY (`hint_id`),
  UNIQUE KEY `UQ_hint_quiz` (`quiz_id`),
  CONSTRAINT `hint_ibfk_1` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`quiz_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quiz`
--

DROP TABLE IF EXISTS `quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz` (
  `quiz_id` bigint NOT NULL AUTO_INCREMENT,
  `quiz_text` varchar(500) NOT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`quiz_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quizoption`
--

DROP TABLE IF EXISTS `quizoption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quizoption` (
  `option_id` bigint NOT NULL AUTO_INCREMENT,
  `quiz_id` bigint NOT NULL,
  `option_text` varchar(100) NOT NULL,
  `option_meaning` varchar(500) NOT NULL,
  `example_text` varchar(500) DEFAULT NULL COMMENT '정답 보기일 때만 예문 제공',
  `answer_number` int NOT NULL COMMENT '보기 순서: 1~4',
  `is_answer` tinyint(1) NOT NULL DEFAULT '0' COMMENT '정답여부(0/1)',
  `is_answer_flag` tinyint GENERATED ALWAYS AS ((case when (`is_answer` = 1) then 1 else NULL end)) STORED,
  PRIMARY KEY (`option_id`),
  UNIQUE KEY `uq_quiz_option_pos` (`quiz_id`,`answer_number`),
  UNIQUE KEY `uq_quiz_option_pair` (`quiz_id`,`option_id`),
  UNIQUE KEY `UQ_QuizOption_one_correct` (`quiz_id`,`is_answer_flag`),
  CONSTRAINT `quizoption_ibfk_1` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`quiz_id`) ON DELETE CASCADE,
  CONSTRAINT `CK_QuizOption_answer_number` CHECK ((`answer_number` between 1 and 4)),
  CONSTRAINT `CK_QuizOption_example_when_correct` CHECK (((`is_answer` = 1) or (`example_text` is null))),
  CONSTRAINT `CK_QuizOption_is_answer` CHECK ((`is_answer` in (0,1)))
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `quizsession`
--

DROP TABLE IF EXISTS `quizsession`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quizsession` (
  `session_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `score` int DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`session_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `quizsession_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `CK_QuizSession_score` CHECK (((`score` is null) or ((`score` between 0 and 100) and ((`score` % 5) = 0))))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sessionanswer`
--

DROP TABLE IF EXISTS `sessionanswer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sessionanswer` (
  `answer_id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `quiz_id` bigint NOT NULL,
  `selected_option_id` bigint NOT NULL,
  `is_correct` tinyint(1) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`answer_id`),
  UNIQUE KEY `UQ_SessionAnswer_once` (`session_id`,`quiz_id`),
  KEY `quiz_id` (`quiz_id`,`selected_option_id`),
  KEY `IX_SA_session_created` (`session_id`,`created_at` DESC),
  CONSTRAINT `sessionanswer_ibfk_1` FOREIGN KEY (`session_id`, `quiz_id`) REFERENCES `sessionquiz` (`session_id`, `quiz_id`) ON DELETE CASCADE,
  CONSTRAINT `sessionanswer_ibfk_2` FOREIGN KEY (`quiz_id`, `selected_option_id`) REFERENCES `quizoption` (`quiz_id`, `option_id`) ON DELETE RESTRICT,
  CONSTRAINT `CK_SA_correct` CHECK ((`is_correct` in (0,1)))
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sessionquiz`
--

DROP TABLE IF EXISTS `sessionquiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sessionquiz` (
  `session_quiz_id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL,
  `quiz_id` bigint NOT NULL,
  `quiz_order` int NOT NULL COMMENT '세션 내 순서 (1~20)',
  PRIMARY KEY (`session_quiz_id`),
  UNIQUE KEY `uq_session_quiz_order` (`session_id`,`quiz_order`),
  UNIQUE KEY `uq_session_quiz_once` (`session_id`,`quiz_id`),
  KEY `quiz_id` (`quiz_id`),
  CONSTRAINT `sessionquiz_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `quizsession` (`session_id`) ON DELETE CASCADE,
  CONSTRAINT `sessionquiz_ibfk_2` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`quiz_id`) ON DELETE CASCADE,
  CONSTRAINT `CK_SessionQuiz_order` CHECK ((`quiz_order` between 1 and 20))
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `nickname` varchar(5) NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `CK_User_nickname_len` CHECK ((char_length(trim(`nickname`)) between 1 and 5)),
  CONSTRAINT `CK_User_nickname_trim` CHECK ((`nickname` = trim(`nickname`)))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-08-16 10:11:42
