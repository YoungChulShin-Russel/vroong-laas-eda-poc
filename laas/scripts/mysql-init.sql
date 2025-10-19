-- Create databases
CREATE DATABASE IF NOT EXISTS `order`;
CREATE DATABASE IF NOT EXISTS `delivery`;
CREATE DATABASE IF NOT EXISTS `dispatch`;

-- Grant privileges to laas_user for all three databases
GRANT ALL PRIVILEGES ON `order`.* TO 'laas_user'@'%';
GRANT ALL PRIVILEGES ON `delivery`.* TO 'laas_user'@'%';
GRANT ALL PRIVILEGES ON `dispatch`.* TO 'laas_user'@'%';

FLUSH PRIVILEGES;