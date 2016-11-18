-- phpMyAdmin SQL Dump
-- version 4.0.10.14
-- http://www.phpmyadmin.net
--
-- Host: localhost:3306
-- Generation Time: Nov 18, 2016 at 01:07 PM
-- Server version: 5.6.34
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
-- Structure for view `diylc_category_view`
--

CREATE ALGORITHM=UNDEFINED DEFINER=`diyfever`@`localhost` SQL SECURITY DEFINER VIEW `diylc_category_view` AS select `c1`.`category_id` AS `category_id`,concat(coalesce(concat(`c2`.`name`,'/'),''),`c1`.`name`) AS `search_name`,concat((case when (`c1`.`parent_id` > 0) then '- ' else '' end),`c1`.`name`) AS `display_name` from (`diylc_category` `c1` left join `diylc_category` `c2` on((`c1`.`parent_id` = `c2`.`category_id`))) where (1 = 1) order by `c1`.`sort_order`;

--
-- VIEW  `diylc_category_view`
-- Data: None
--


/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
