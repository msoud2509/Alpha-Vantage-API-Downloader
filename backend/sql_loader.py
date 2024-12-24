import psycopg2 as psy
from sqlalchemy import create_engine
import pandas as pd

SQL_PASSWORD = ""

def create_table(table_name: str, data_types: list, pk_index: int) -> None:
    # connect to database
    conn = psy.connect(host = "localhost", database = "investing", user = "postgres", password = SQL_PASSWORD)
    if conn:
        print("psycopg2 connection successful")

    # create initial table
    cur = conn.cursor()
    create_table_script = f"CREATE TABLE IF NOT EXISTS {table_name}()"
    cur.execute(create_table_script)

    # add columns and make specified column the primary key
    for col_name, col_type in data_types:
        add_col = f"ALTER TABLE {table_name} ADD COLUMN {col_name} {col_type};"
        cur.execute(add_col)
    
    add_pk = f"ALTER TABLE {table_name} ADD PRIMARY KEY ({data_types[pk_index][0]})"
    cur.execute(add_pk)
    
    conn.commit()
    cur.close()
    conn.close()


def load_data(dataf: pd.DataFrame, table_name: str) -> None:
    conn_db = f"postgresql://postgres:{SQL_PASSWORD}@localhost:5432/investing"
    if conn_db:
        print("sqlalchemy connection successful")
    engine = create_engine(conn_db)
    dataf.to_sql(table_name, engine, if_exists = "append", index = False)