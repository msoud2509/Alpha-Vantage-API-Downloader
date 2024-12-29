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
        if header == "Alpha Intelligence™": # temporary measure, probably will need to add these functions manually
            continue
        function_dict[header] = []
        section_code_divs = section.findAll("div", {"class":"python-code"}) # find all code tags to extract functions names
        section_function_titles = section.findAll("h4")
        try:
            assert len(section_code_divs) == len(section_function_titles)
        except AssertionError:
            print("function names are not lining up with their respective titles")
        
        #find all function names and title of section here
        for code, function_title in zip(section_code_divs, section_function_titles):
            section_code = code.find("code").get_text()
            start = section_code.find("function=") + len("function=")
            end = section_code.find("&")

            # format text of title
            title = function_title.get_text()
            if "Premium" in title:
                title = title.replace("Premium", "")
                title = "PREMIUM ONLY - " + title
            if "Trending" in title:
                title = title.replace("Trending", "(Trending)")
            title = title.strip()

            params = get_params(function_title)
            function_dict[header].append({"name":title, "function":section_code[start:end], "parameters":params}) # this is function name 
    
    with open("src/main/resources/functions.json", "w") as f:
        json.dump(function_dict, f, indent=4)

def get_params(header_tag):
    params_lst = []
    p_tags = header_tag.find_next()
    while p_tags.name != "h4" and p_tags.name != "div": # stop at next header (div so last section doesn't throw an error)
        if p_tags.name == "p" and ("❚ Required:" in p_tags.get_text() or "❚ Optional:" in p_tags.get_text()):
            # format text to be just parameter name
            param = p_tags.get_text()
            if "function" in param or "apikey" in param:
                p_tags = p_tags.find_next()
                continue # function and apikey are already input fields
            param = param.replace("❚ Required:", "")
            param = param.replace("❚ Optional:", "")
            param = param.strip()
            params_lst.append(param)
        p_tags = p_tags.find_next()
    return params_lst


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
    
    
    


