import requests
import pandas as pd
import io
import os
# IMPLEMENT THIS LATER!!! from sql_loader import create_table, load_data
from flask import Flask, request, send_file
import csv
import logging

app = Flask(__name__)
logging.basicConfig(filename='debug.log', level=logging.DEBUG)

# parameters to retrieve for api request
# ticker, api_key, function

STOCK_DATA_TYPES = [("timestamp", "DATE"), ("open", "DECIMAL(10, 2)"), ("high", "DECIMAL(10, 2)"), ("low", "DECIMAL(10, 2)"),
                    ("close", "DECIMAL(10, 2)"), ("adjusted_close", "DECIMAL(10, 2)"), ("volume", "INTEGER"), ("dividend_amount", "DECIMAL(5, 2)")]

# # @app.route("/parameters", methods = ["GET"])
# def alpha_vantage_test():
#     # get parameters from frontend using flask request
#     ticker = request.args.get("symbol")
#     function = request.args.get("function")
#     api_key = request.args.get("API Key")

#     url = f"https://www.alphavantage.co/query?function={function}&symbol={ticker}&apikey={api_key}&datatype=csv"
#     req = requests.get(url)
#     dataf = pd.read_csv(io.BytesIO(req.content))
#     dataf.rename(columns = {"adjusted close":"adjusted_close", "dividend amount":"dividend_amount"}, inplace = True)
#     return dataf

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
    app.run(host = "127.0.0.1", port = 5000, debug = True)
    
    


