package com.protocol180.aggregator.cordapp.host;

import com.r3.conclave.mail.Curve25519PrivateKey;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

public class MockClientUtil {
    public static Schema initializeSchema(){
        File schemaFile = new File(ClassLoader.getSystemClassLoader().getResource("envelope.avsc").getPath());
        Schema schema;
        try{
            schema = new Schema.Parser().parse(schemaFile);
        }
        catch (Exception e){
            schema = null;
        }
        return  schema;
    }

    public static byte[] createMailForIdentities(Map<Curve25519PrivateKey, String> clients) throws IOException {
        ArrayList<GenericRecord> genericRecords = new ArrayList<>();
        Schema identitySchema = initializeSchema().getField("identity").schema();
        for (Map.Entry<Curve25519PrivateKey,String> clientEntry : clients.entrySet()){

            GenericRecord demandRecord = new GenericData.Record(identitySchema);
            demandRecord.put("publicKey", Base64.getEncoder().encodeToString(clientEntry.getKey().getPublicKey().getEncoded()));
            demandRecord.put("clientType",clientEntry.getValue());
            genericRecords.add(demandRecord);
        }

        File clientIdentitiesFile = createAvroDataFileFromGenericRecords(identitySchema, genericRecords, "identities.avro");
        return Files.readAllBytes(clientIdentitiesFile.toPath());
    }

    public static File createAvroDataFileFromGenericRecords(Schema schema, ArrayList<GenericRecord> genericRecords, String filename) throws IOException {
        File file = new File(filename);
        DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter);
        dataFileWriter.create(schema, file);
        genericRecords.forEach(genericRecord -> {
            try {
                dataFileWriter.append(genericRecord);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        dataFileWriter.close();
        return file;
    }

}
