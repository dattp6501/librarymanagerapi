create table group_(
	id integer primary key auto_increment,
    group_name varchar(255) not null unique,
    note nvarchar(255)
);
alter table group_ auto_increment = 1;

create table members(
	id integer primary key auto_increment,
    fullname nvarchar(255) not null,
    email varchar(100) not null unique,
    username varchar(50) not null unique,
    passwd varchar(20) not null,
    group_id integer,
    image longblob,
    address text,
    foreign key(group_id) references group_(id)
);
alter table members auto_increment = 1;

create table books(
	id integer primary key auto_increment,
    title nvarchar(255) not null unique,
    author nvarchar(255),
    typeb nvarchar(255),
    release_date date,
    create_date date,
    page_number integer not null,
    image longblob,
    price float not null,
    number integer not null,
    description text
);

create table type_book(
	id integer primary key auto_increment,
    name nvarchar(255) not null unique,
    note text
);

create table type_of_book(
	book_id integer not null,
    type_id integer not null,
    foreign key(book_id) references books(id),
    foreign key(type_id) references type_book(id)
);

create table comment(
	book_id integer,
    member_id integer,
    star integer not null,
    content text,
    date datetime not null,
    foreign key(book_id) references books(id),
    foreign key(member_id) references members(id),
);
alter table books auto_increment = 1;

create table booking(
	id integer primary key auto_increment,
    member_id integer not null,
    date datetime not null,
    address text not null,
    pay float not null,
    tatal_price float not null,
    success int not null,
    note text,
    foreign key(member_id) references members(id)
);
alter table booking auto_increment = 1;

create table voucher_booking(
	id integer primary key auto_increment,
    name nvarchar(255) not null unique,
    type varchar(10) not null, -- %(giam theo phan tram),n(giam theo gia co dinh)
    value float not null,
    created_date datetime not null,
    note text,
    active integer not null
);
alter table voucher_booking auto_increment = 1;

create table voucher_of_booking(
	booking_id integer not null,
    voucher_booking_id integer not null,
    foreign key(booking_id) references booking(id),
    foreign key(voucher_booking_id) references voucher_booking(id)
);

create table booked(
	booking_id integer not null,
	book_id integer not null,
    price float not null,
    number integer not null,
    note text,
    foreign key(book_id) references books(id),
    foreign key(booking_id) references booking(id),
);


create table cart(
    id integer primary key auto_increment,
	member_id integer not null,
    foreign key(member_id) references members(id)
);
alter table cart auto_increment = 1;

create table product_of_cart(
	cart_id integer not null,
    book_id integer not null,
    book_number integer,
    foreign key(cart_id) references cart(id),
    foreign key(book_id) references books(id)
);


create table member_login(
	session_id varchar(50) primary key,
    member_id integer not null,
    time_to datetime not null
);


