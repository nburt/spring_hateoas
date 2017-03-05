CREATE TABLE bookmark (
  id BIGINT not null AUTO_INCREMENT,
  description VARCHAR(255),
  uri VARCHAR(255),
  account_id BIGINT,
  PRIMARY KEY (id)
)