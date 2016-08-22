create table md[0](row_id) as
    select d.row_id
    from data as d;

create table md[i](row_id) as
    select d.row_id +1
    from md[i-1] as d;

create table md[3*n + 2](row_id) as
    select d.row_id + 2
    from md[i-1] as d;
