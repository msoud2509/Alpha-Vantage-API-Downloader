import re
import requests
import pandas as pd
import io
import os
# IMPLEMENT THIS LATER!!! from sql_loader import create_table, load_data
from flask import Flask, jsonify, request, send_file
import logging
import json

from bs4 import BeautifulSoup

app = Flask(__name__)
logging.basicConfig(filename='debug.log', level=logging.DEBUG)

# parameters to retrieve for api request
# ticker, api_key, function

@app.route("/load_functions", methods=["GET"])
def scrape_doc_headers():
    response = requests.get("https://www.alphavantage.co/documentation/")
    soup = BeautifulSoup(response.content, "html.parser")
    main_content = soup.find("article", {"class":"main-content"})
    function_dict = {}
    for i in range(len(main_content.findAll("section"))):
        section = main_content.findAll("section")[i]
        header = section.find("h2").get_text()
        if header == "Alpha Intelligence™":
            continue
        function_dict[header] = []
        section_code_div = section.findAll("div", {"class":"python-code"})
        for code in section_code_div:
            section_code = code.find("code").get_text()
            start = section_code.find("function=") + len("function=")
            end = section_code.find("&")
            function_dict[header].append(section_code[start:end]) 
    with open("src/main/resources/functions.json", "w") as f:
        json.dump(function_dict, f, indent=4)
    print(len(function_dict))

    
    

STOCK_DATA_TYPES = [("timestamp", "DATE"), ("open", "DECIMAL(10, 2)"), ("high", "DECIMAL(10, 2)"), ("low", "DECIMAL(10, 2)"),
                    ("close", "DECIMAL(10, 2)"), ("adjusted_close", "DECIMAL(10, 2)"), ("volume", "INTEGER"), ("dividend_amount", "DECIMAL(5, 2)")]

@app.route("/download", methods = ["POST"])
def get_alpha_vantage_data():
    app.logger.debug('download route accessed')
    data = request.get_json()
    app.logger.debug(f"Recieved parameters: {data}")
    # Extract data
    symbol = data.get('symbol')
    api_key = data.get('api_key')
    
    url = f"https://www.alphavantage.co/query?function=TIME_SERIES_MONTHLY_ADJUSTED&symbol={symbol}&apikey={api_key}&datatype=csv"
    req = requests.get(url)
    app.logger.debug("Retrieved data successfully")
    dataf = pd.read_csv(io.BytesIO(req.content))

    csv_output = io.StringIO()
    dataf.to_csv(csv_output, index=False)
    csv_output.seek(0)
    app.logger.debug("Data saved successfully")
    return send_file(io.BytesIO(csv_output.getvalue().encode()), 
                     as_attachment=True,  
                     mimetype='text/csv',
                     download_name="test_file.csv")

if __name__ == "__main__":
    # app.run(host = "127.0.0.1", port = 5000, debug = True)
    scrape_doc_headers()
    
    


