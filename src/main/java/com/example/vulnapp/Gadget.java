package com.example.vulnapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.logging.Logger;

public class Gadget implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(Gadget.class.getName());

    private String label;

    public Gadget() {
    }

    public Gadget(String label) {
        this.label = label;
    }

    // VULN: Insecure Deserialization - custom readObject runs on untrusted object streams
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        LOG.info("Gadget deserialized (training only): " + label);
    }
}
