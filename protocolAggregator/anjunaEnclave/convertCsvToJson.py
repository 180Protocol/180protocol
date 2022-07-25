import csv
import json
 
 
# Function to convert a CSV to JSON
# Takes the file paths as arguments
def make_json(csvFilePath):
     
    # create a dictionary
    jsonArray = []
     
    # Open a csv reader called DictReader
    with open(csvFilePath, encoding='utf-8') as csvf:
        csvReader = csv.DictReader(csvf)
        print(csvf)
         
        # Convert each row into a dictionary
        # and add it to data
        for row in csvReader:
             
            jsonArray.append(row)
 
        return jsonArray        
