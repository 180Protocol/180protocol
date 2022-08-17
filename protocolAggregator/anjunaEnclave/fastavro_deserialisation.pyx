import avro.schema
import convertCsvToJson
import datetime
from avro.datafile import DataFileReader, DataFileWriter
from avro.io import DatumReader, DatumWriter
from fastavro import writer, reader, parse_schema

start = datetime.datetime.now()

# Reading
with open('dataOutput.avro', 'rb') as fo:
    for record in reader(fo):
        print(record)

print(start)
print(datetime.datetime.now())