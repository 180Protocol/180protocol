package com.protocol180.aggregator.flow

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.apache.avro.Schema
import org.apache.avro.file.DataFileReader
import org.apache.avro.file.DataFileWriter
import org.apache.avro.file.SeekableByteArrayInput
import org.apache.avro.file.SeekableInput
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumReader
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumReader
import org.apache.avro.io.DatumWriter
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Consumer

/**
 * A utility service to provide functionality of avro serialization, creating data input for providers and
 * generating response records from decrypted output
 */
@CordaService
class EnclaveClientService(val services: AppServiceHub) : SingletonSerializeAsToken() {


    var envelopeSchema: Schema? = null
    var aggregationInputSchema: Schema? = null
    var aggregationOutputSchema: Schema? = null
    var provenanceOutputSchema: Schema? = null
    var identitySchema: Schema? = null

    init {
        envelopeSchema = initializeSchema()
        aggregationInputSchema = envelopeSchema!!.getField("aggregateInput").schema()
        aggregationOutputSchema = envelopeSchema!!.getField("aggregateOutput").schema()
        provenanceOutputSchema = envelopeSchema!!.getField("provenanceOutput").schema()
        identitySchema = envelopeSchema!!.getField("identity").schema()
    }

    private fun initializeSchema(): Schema? {
        val schema: Schema? = try {
            Schema.Parser().parse(javaClass.getResourceAsStream("/envelope.avsc"))
        } catch (e: Exception) {
            null
        }
        return schema
    }


    fun createProviderDataRecordForAggregation(): ByteArray? {
        //create generic records using avro schema for aggregation and append to file
        val records = createGenericSchemaRecords(aggregationInputSchema)
        val dataFileForAggregation = createAvroDataFileFromGenericRecords(aggregationInputSchema, records, "aggregate.avro")
        return Files.readAllBytes(dataFileForAggregation.toPath())
    }

    private fun createAvroDataFileFromGenericRecords(schema: Schema?, genericRecords: ArrayList<GenericRecord>, filename: String?): File {
        val file = File(filename)
        val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(schema)
        val dataFileWriter = DataFileWriter(datumWriter)
        dataFileWriter.create(schema, file)
        genericRecords.forEach(Consumer { genericRecord: GenericRecord ->
            try {
                dataFileWriter.append(genericRecord)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
        dataFileWriter.close()
        return file
    }

    private fun createGenericSchemaRecords(schema: Schema?): ArrayList<GenericRecord> {
        val genericRecords = ArrayList<GenericRecord>()
        for (i in 0..1) {
            genericRecords.add(generateRandomDemandRecord(schema))
        }
        return genericRecords
    }

    private fun generateRandomDemandRecord(schema: Schema?): GenericRecord {
        val creditRatings = arrayOf("A", "AA", "AAA", "B", "C")
        val sectors = arrayOf("FINANCIALS", "INDUSTRIALS", "IT", "INFRASTRUCTURE", "ENERGY")
        val assetTypes = arrayOf("B", "PP", "L")
        val duration = arrayOf("1", "2", "3", "4", "5")
        val demandRecord: GenericRecord = GenericData.Record(schema)
        val randomizer = Random()
        demandRecord.put("creditRating", creditRatings[randomizer.nextInt(creditRatings.size)])
        demandRecord.put("sector", sectors[randomizer.nextInt(sectors.size)])
        demandRecord.put("assetType", assetTypes[randomizer.nextInt(assetTypes.size)])
        demandRecord.put("duration", duration[randomizer.nextInt(duration.size)])
        demandRecord.put("amount", ThreadLocalRandom.current().nextInt(1000000, 1000000000 + 1))
        return demandRecord
    }

    fun readGenericRecordsFromOutputBytesAndSchema(outputBytes: ByteArray, schemaType: String): ArrayList<GenericRecord?> {
        val datumReader: DatumReader<GenericRecord> = if (schemaType == "aggregate") GenericDatumReader(aggregationInputSchema) else GenericDatumReader(provenanceOutputSchema)
        val input: SeekableInput = SeekableByteArrayInput(outputBytes)
        val dataFileReader = DataFileReader(input, datumReader)
        val genericRecords = ArrayList<GenericRecord?>()
        var dataRecord: GenericRecord? = null
        while (dataFileReader.hasNext()) {
            dataRecord = dataFileReader.next()
            println("Record: $dataRecord")
            genericRecords.add(dataRecord)
        }
        return genericRecords
    }
}
