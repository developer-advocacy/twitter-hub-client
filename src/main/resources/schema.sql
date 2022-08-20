-- drop table spring_teammate;

create table if not exists spring_teammates
(

    url      text             not null,
    name     text primary key not null,
    position text             not null,
    location text             not null,
    github   text,
    twitter  text
);


create table spring_blog_posts
(
    url        text      not null,
    published  timestamp not null,
    categories text[],
    author     text references spring_teammates (name)

);