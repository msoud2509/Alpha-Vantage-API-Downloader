from bs4 import BeautifulSoup
import pandas as pd
from sql_loader import create_table, load_data
import requests

TABLE_NAME = "sp_500"

def getSP500() -> pd.DataFrame:
    url = "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies#S&P_500_component_stocks"
    wiki_soup = BeautifulSoup(requests.get(url).content, "lxml")
    table = wiki_soup.find("table")
    dataf = pd.read_html(str(table))[0]
    dataf.rename(columns = {"GICS Sector":"GICS_Sector", "GICS Sub-Industry":"GICS_Sub_Industry", "Headquarters Location":"Headquarters_Location",
                            "Date added":"Date_added"}, inplace=True)
    dataf.columns = dataf.columns.str.lower()
    return dataf


SP500_DATA_TYPES = [("Symbol", "VARCHAR(15)"), ("Security", "TEXT"), ("GICS_Sector", "TEXT"), ("GICS_Sub_Industry", "TEXT"), 
                    ("Headquarters_Location", "TEXT"), ("Date_added", "VARCHAR(10)"), ("CIK", "INTEGER"), ("Founded", "TEXT")]

# if __name__ == "__main__":
#     create_table(table_name = "sp_500", data_types = SP500_DATA_TYPES, pk_index = 0)
#     load_data(dataf = getSP500, table_name = TABLE_NAME)