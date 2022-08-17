### Introduction

AnjunaEnclave module for intel SGx enclave managment with Anjuna Development & Runtime Kit.  [For more info on Anjuna Platform](https://docs.r3.com/) 

### Benchmark for speed in different environment

CSV data file used : [Download](https://stats.govt.nz/assets/Uploads/New-Zealand-business-demography-statistics/New-Zealand-business-demography-statistics-At-February-2021/Download-data/Geographic-units-by-industry-and-statistical-area-2000-2021-descending-order-CSV.zip)  

Avro lib used to serialize above csv data: [Avro-Python](https://fastavro.readthedocs.io/en/latest/)  
After serialization approximated size of avro file is 123 MB

* Local machine with specs: Without SGx Chip, 16G RAM, AMD Hexa core(3200 MHz)   
 Performance:  
    Approx time to deserialize recrods: ~ 6 Min  
    Number of serialised avro records in file: 5704247 or ~ 5.5 Mn  
    

* Azure SGx VM with specs: With SGx chip, 16G RAM, Standard DC4s v2, Quod core   
 Performance:  
    Approx time to deserialize recrods: ~ 25 Min  
    Number of serialised avro records in file: 5704247 or ~ 5.5 Mn  
    Note: With SGx Anjuna decryption also included in process.  
