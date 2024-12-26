import requests
import pandas as pd
import io
import os
# IMPLEMENT THIS LATER!!! from sql_loader import create_table, load_data
from flask import Flask, request

app = Flask(__name__)

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
def test_get_and_send():
    data = request.get_json()

    # Extract data
    symbol = data.get('symbol')
    function = data.get('function')
    api_key = data.get('api_key')

    return f"{symbol}, {function}, {api_key}: success!"

if __name__ == "__main__":
    print(os.getpid())
    app.run(host = "127.0.0.1", port = 5000, debug = False)
    

