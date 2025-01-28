import os
from flask import Flask, request, jsonify
from flask_cors import CORS
from paddleocr import PaddleOCR
import numpy as np
import requests  # for currency conversion
import time

app = Flask(__name__)
CORS(app)  # enable CORS for all routes



# Global variable to store currency rates and the timestamp of when they were last updated
currency_cache = {
    "rates": None,
    "last_updated": 0
}

CACHE_EXPIRY_TIME = 3600  # Cache expiry time in seconds (1 hour)


# -----------------------------
# 1. TEST ROUTE
# -----------------------------
@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "Welcome to the Expense Tracker Backend!"}), 200


# -----------------------------
# 2. OCR ROUTE
# -----------------------------
@app.route("/api/ocr", methods=["POST"])
def ocr_receipt():
    """
    Receives an image file from the request, processes it with PaddleOCR,
    and returns the extracted text.
    """

    if "file" not in request.files:
        return jsonify({"error": "No file part in request"}), 400

    file = request.files["file"]
    if file.filename == "":
        return jsonify({"error": "No selected file"}), 400

    try:
        # Initialize the PaddleOCR model
        ocr = PaddleOCR(use_angle_cls=True, lang='it')

        # Perform OCR on an image
        result = ocr.ocr(file)

        if not result:
            return jsonify({"error": "No text found in the image"}), 404

        # Extract text from OCR result
        extracted_text = "\n".join([line[1][0] for line in result[0]])

        # Check if the OCR extraction is successful
        if not extracted_text.strip():
            return jsonify({"error": "No text found in the image"}), 404

        return jsonify({"extracted_text": extracted_text}), 200

    except Exception as e:
        # Handle errors during image processing or OCR
        return jsonify({"error": f"Error processing the image: {str(e)}"}), 500


# -----------------------------
# 3. EXPENSE CATEGORIZATION
# -----------------------------
@app.route("/api/categorize", methods=["POST"])
def categorize_receipt():
    """
    Accepts JSON data containing extracted text or user input,
    returns a best-guess category (e.g., 'Food', 'Transport', etc.).
    """

    data = request.get_json()
    if not data:
        return jsonify({"error": "No JSON data provided"}), 400

    text = data.get("text", "")
    if not text:
        return jsonify({"error": "No text provided"}), 400

    # Define keyword categories and their weights (in Italian)
    categories = {
        "Food": {
            "keywords": ["ristorante", "cibo", "pizza", "hamburger", "gnocchi", "pasta", "pranzo", "vino", "caffè"],
            "weights": [2, 2, 3, 3, 3, 2, 2, 1, 1]  # Weight for each keyword
        },
        "Transport": {
            "keywords": ["uber", "taxi", "autobus", "treno", "volo", "carburante"],
            "weights": [3, 3, 2, 2, 2, 3]
        },
        "Entertainment": {
            "keywords": ["film", "concerto", "teatro", "evento", "spettacolo", "biglietto", "gioco"],
            "weights": [3, 3, 3, 2, 2, 1, 1]
        }
    }

    # Initialize a dictionary to keep track of category scores
    category_scores = {category: 0 for category in categories}

    # Process the text and calculate scores for each category
    text_lower = text.lower()
    for category, details in categories.items():
        for keyword, weight in zip(details["keywords"], details["weights"]):
            if keyword in text_lower:
                category_scores[category] += weight

    # Find the category with the highest score
    best_category = max(category_scores, key=category_scores.get)

    # Return the best guess category
    return jsonify({"category": best_category}), 200


# -----------------------------
# 4. Currency Conversion
# -----------------------------
@app.route("/api/convert", methods=["POST"])
def convert_currency():
    """
    Expects JSON with:
        amount: float
        from_currency: str
        to_currency: str
    Returns the converted amount using an external API or cached data.
    """
    # Parse the input JSON
    data = request.json
    amount = data.get("amount")
    from_currency = data.get("from_currency")
    to_currency = data.get("to_currency")

    if not amount or not from_currency or not to_currency:
        return jsonify({"error": "Missing required parameters: amount, from_currency, to_currency"}), 400

    # Check if the cache has expired
    current_time = time.time()
    if currency_cache["rates"] is None or current_time - currency_cache["last_updated"] > CACHE_EXPIRY_TIME:
        # Cache has expired or is empty, so fetch fresh data
        try:
            api_key = "6769c87a9c2845978189d54ea6a39f63"
            url = "https://openexchangerates.org/api/latest.json"
            params = {"app_id": api_key}
            response = requests.get(url, params=params)
            response.raise_for_status()  # Raise exception for HTTP errors
            rates = response.json().get("rates")

            if from_currency not in rates or to_currency not in rates:
                return jsonify({"error": f"Unsupported currency: {from_currency} or {to_currency}"}), 400

            # Update the cache with the new data
            currency_cache["rates"] = rates
            currency_cache["last_updated"] = current_time

        except requests.exceptions.RequestException as e:
            return jsonify({"error": f"Error fetching exchange rates: {str(e)}"}), 500

    # Use cached rates for conversion
    rates = currency_cache["rates"]
    from_rate = rates[from_currency]
    to_rate = rates[to_currency]
    conversion_rate = to_rate / from_rate

    # Convert the amount
    converted_amount = amount * conversion_rate

    return jsonify({
        "amount": amount,
        "from_currency": from_currency,
        "to_currency": to_currency,
        "converted_amount": round(converted_amount, 2),
        "conversion_rate": round(conversion_rate, 6)
    })

# -----------------------------
# MAIN
# -----------------------------
if __name__ == "__main__":
    # For local development
    # app.run(debug=True, host="0.0.0.0", port=5000)

    # If you have an environment variable for the port (e.g., on Heroku):
    port = int(os.environ.get("PORT", 5000))
    app.run(debug=True, host="0.0.0.0", port=port)
