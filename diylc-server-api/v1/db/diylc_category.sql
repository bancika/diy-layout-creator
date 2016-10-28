-- phpMyAdmin SQL Dump
-- version 4.0.10.14
-- http://www.phpmyadmin.net
--
-- Host: localhost:3306
-- Generation Time: Oct 28, 2016 at 10:45 AM
-- Server version: 5.6.33
-- PHP Version: 5.6.20

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `diyfever_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `diylc_category`
--

CREATE TABLE IF NOT EXISTS `diylc_category` (
  `category_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) NOT NULL,
  `sort_order` int(11) NOT NULL DEFAULT '0',
  `parent_id` int(11) NOT NULL,
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `category_id` (`category_id`),
  KEY `category_id_2` (`category_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=22 ;

--
-- Dumping data for table `diylc_category`
--

INSERT INTO `diylc_category` (`category_id`, `name`, `sort_order`, `parent_id`) VALUES
(1, 'Effects', 200, 0),
(2, 'Compressors', 210, 1),
(3, 'Delays', 215, 1),
(4, 'Distortions', 220, 1),
(5, 'Reverbs', 235, 1),
(6, 'Boosters', 205, 1),
(7, 'Wahs', 240, 1),
(8, 'Modulations', 225, 1),
(9, 'Amplifiers', 100, 0),
(13, 'Hi-Fi Amplifiers', 150, 9),
(14, 'Guitar Amplifiers', 110, 9),
(21, 'Hi-Fi Preamps', 160, 9),
(20, 'Guitar Preamps', 120, 9),
(18, 'Guitar Wiring Diagrams', 400, 0),
(19, 'Other', 1000, 0);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
