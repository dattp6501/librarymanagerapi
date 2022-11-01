create table books(
	id integer primary key auto_increment,
    title nvarchar(255) not null,
    author nvarchar(255),
    typeb nvarchar(255),
    release_date date,
    page_number integer not null
);
alter table books auto_increment = 1;
create table members(
	id integer primary key auto_increment,
    fullname nvarchar(255) not null,
    email varchar(100) unique not null,
    username varchar(50) not null unique,
    passwd varchar(20) not null
);
alter table members auto_increment = 1;