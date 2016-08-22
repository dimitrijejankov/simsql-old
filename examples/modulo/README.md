##Modulo tables

Modulo tables are a kind of general index table that allows you to use a different definition of the random table for a particular subset of indexes.
Every modulo table has the has a very similar definition to a normal general index table.

~~~~
-- general index table
--
create table[i]
    select statment...

-- modulo table
--
create table[3*n + 2]
    select statement...
~~~~

So when a user decides do a from the table
~~~~
select * from table[i];
~~~~

If such **n** exists that **i = 5*n + 2**, the table definition for the table[5*n + 2] will be used to execute the select statement. Otherwise the definition for the table[i] is used.

## How to run the example

Run the simsql shell inside the simsql directory, and enter the following commands in the given order to create the schemas.

~~~~
run examples/modulo/modulo_pre.sql
run examples/modulo/modulo.sql
run examples/modulo/fill_data.sql
~~~~

After this is done you can do a select statement on the created table. For example :
~~~~
select * from md[3];
~~~~

1. It will first calculate **md\[0\]** from the definition for **md\[0\]** with the data from the **data** table.
2. After that it will calculate **md\[1\]** from the definition for the **md\[i\]** table with the data from **md\[0\]**
3. After that it will calculate **md\[2\]** from the definition for the **md\[3*n + 2\]** table with the data from **md\[1\]**
4. After that it will calculate **md\[3\]** from the definition for the **md\[i\]** table with the data from the **md\[2\]** and output the data to the terminal.