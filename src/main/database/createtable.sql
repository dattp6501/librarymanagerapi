create table books(
	id integer primary key auto_increment,
    title nvarchar(255) not null,
    author nvarchar(255),
    typeb nvarchar(255),
    release_date date,
    page_number integer not null,
    image longblob
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
-- ---------- BTL 2----------------
create table groups(
	id integer primary key auto_increment,
    name varchar(20) not null,
    note nvarchar(255)
);
alter table groups auto_increment = 1;

create table membergroup(
	id integer primary key auto_increment,
	member_id integer not null,
    group_id integer not null,
    foreign key(member_id) references members(id),
    foreign key(group_id) references groups(id)
);
alter table membergroup auto_increment = 1;

create table permission(
	id integer primary key auto_increment,
    name varchar(30) not null unique,
    note nvarchar(255)
);
alter table permission auto_increment = 1;

create table permissiongroup(
	id integer primary key auto_increment,
    group_id integer not null,
    permission_id integer not null,
    foreign key(group_id) references groups(id),
    foreign key(permission_id) references permission(id)
);
alter table permissiongroup auto_increment = 1;