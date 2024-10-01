package com.dylibso.examples.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.extism.chicory.sdk.Manifest;
import org.extism.chicory.sdk.ManifestWasm;
import org.extism.chicory.sdk.Plugin;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;

public class KafkaFilter {
    public static KafkaFilter fromInputStream(XTPService.Extension ext, InputStream is) throws IOException {
        ManifestWasm wasm = ManifestWasm.fromBytes(is.readAllBytes()).build();
        Plugin plugin = Plugin.ofManifest(Manifest.ofWasms(wasm).build()).build();
        return new KafkaFilter(plugin, ext);
    }

    private static final Logger logger = Logger.getLogger(KafkaFilter.class);

    private final Plugin plugin;
    private final XTPService.Extension extension;

    public KafkaFilter(Plugin plugin, XTPService.Extension extension) {
        this.plugin = plugin;
        this.extension = extension;
    }

    public XTPService.Extension extension() {
        return extension;
    }

    public Record transform(Record record, ObjectMapper mapper) throws IOException {
        return mapper.readValue(transformBytes(mapper.writeValueAsBytes(record)), Record.class);
    }

    public byte[] transformBytes(byte[] recordBytes) {
        logger.infof("transforming: %s", new String(recordBytes));
        byte[] result = plugin.call("transform", recordBytes);
        logger.infof("result: %s", new String(result));
        return result;
    }

}
