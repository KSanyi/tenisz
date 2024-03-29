CREATE TABLE TENISZ_USER(USERID VARCHAR(50), PASSWORD_HASH VARCHAR(100) NOT NULL, NAME VARCHAR(100) NOT NULL, ROLE VARCHAR(20) NOT NULL, PHONE VARCHAR(20), EMAIL VARCHAR(100), STATUS VARCHAR(20) NOT NULL, PLAYER_ID INT, PRIMARY KEY (USERID));

CREATE TABLE TENISZ_PLAYER(ID INT AUTO_INCREMENT, NAME VARCHAR(100) NOT NULL, EMAIL VARCHAR(50) NOT NULL DEFAULT '', PHONE VARCHAR(50) NOT NULL DEFAULT '', ZIP INT, TOWN VARCHAR(50), STREET_ADDRESS VARCHAR(200), COMMENT VARCHAR(500) NOT NULL DEFAULT '', UTR_GROUP DECIMAL(5,2), ORGS VARCHAR(100), PRIMARY KEY (ID));

CREATE TABLE TENISZ_TOURNAMENT(ID VARCHAR(8) PRIMARY KEY, ORGANIZER VARCHAR(20) NOT NULL, NAME VARCHAR(100) NOT NULL, VENUE VARCHAR(20), NUMBER_OF_COURTS INT, SURFACE VARCHAR(20), VENUE_TYPE VARCHAR(20), DATE DATE, TYPE VARCHAR(50) NOT NULL, STATUS VARCHAR(50) NOT NULL, BEST_OF_N_SETS INT NOT NULL, STRUCTURE VARCHAR(50) NOT NULL, LEVEL_FROM VARCHAR(4) NOT NULL, LEVEL_TO VARCHAR(4) NOT NULL, DESCRIPTION VARCHAR(1000));

CREATE TABLE TENISZ_TOURNAMENT_REGISTRATION(TOURNAMENT_ID VARCHAR(8), PLAYER_ID VARCHAR(50), DATETIME DATETIME NOT NULL, PRIMARY KEY(TOURNAMENT_ID, PLAYER_ID));

CREATE TABLE TENISZ_TOURNAMENT_CONTESTANT(TOURNAMENT_ID VARCHAR(8), PLAYER_ID VARCHAR(50), RANK_NUMBER INT, POSITION INT, PAYMENT_STATUS VARCHAR(20) NOT NULL, PRIMARY KEY(TOURNAMENT_ID, PLAYER_ID));

CREATE TABLE TENISZ_TENNIS_MATCH(ID INT PRIMARY KEY AUTO_INCREMENT, TOURNAMENT_ID VARCHAR(8), TOURNAMENT_BOARD_NUMBER INT, TOURNAMENT_MATCH_NUMBER INT, PLAYER1_ID VARCHAR(50), PLAYER2_ID VARCHAR(50), DATETIME DATETIME, RESULT VARCHAR(50), PLAYER1_UTR DECIMAL(5,2), PLAYER2_UTR DECIMAL(5,2), MATCH_UTR_FOR_PLAYER1 DECIMAL(5,2), MATCH_UTR_FOR_PLAYER2 DECIMAL(5,2));

CREATE TABLE TENISZ_REGISTRATION(ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(100) NOT NULL, EMAIL VARCHAR(50) NOT NULL, PHONE VARCHAR(50) NOT NULL, ZIP INT NOT NULL, TOWN VARCHAR(50) NOT NULL, STREET_ADDRESS VARCHAR(200) NOT NULL, EXPERIENCE VARCHAR(200) NOT NULL, PLAY_FREQUENCY VARCHAR(200) NOT NULL, VENUE VARCHAR(200) NOT NULL, HAS_PLAYED_TOURNAMENT VARCHAR(100) NOT NULL, STATUS VARCHAR(30) NOT NULL, TIMESTAMP TIMESTAMP NOT NULL);
