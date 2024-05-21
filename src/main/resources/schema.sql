create table game(
    id serial primary key,
    width int,
    height int,
    board varchar(2500),
    player1 int,
    player2 int,
    turn int
);
create table player(
    id serial primary key,
    name varchar(50),
    win int
);
