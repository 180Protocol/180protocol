import avro.schema
import convertCsvToJson
from avro.datafile import DataFileReader, DataFileWriter
from avro.io import DatumReader, DatumWriter

schema: avro.schema.RecordSchema = avro.schema.parse(open("schema.avsc", "rb").read())

inputSchema: avro.schema.RecordSchema = schema._props.get("fields")[0].props.get('type')

inputRecords = convertCsvToJson.make_json("ProviderInputData.csv")

# print(convertCsvToJson.make_json("ProviderInputData.csv")[0])
# print(inputSchema)

# writer = DataFileWriter(open("aggregateOutput.avro", "wb"), DatumWriter(), inputSchema)
# for record in inputRecords:
#     print(record)
#     writer.append(record)
    
# # writer.append({"name": "Alyssa", "favorite_number": 256})
# # writer.append({"name": "Ben", "favorite_number": 7, "favorite_color": "red"})
# writer.close()

reader = DataFileReader(open("aggregateOutput.avro.encrypted", "rb"), DatumReader())
for record in reader:
    print(record)

reader.close()