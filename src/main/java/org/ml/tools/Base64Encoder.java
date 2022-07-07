/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ml.tools;

import java.util.Base64;
import org.ml.options.OptionData;
import org.ml.options.Options;

/**
 *
 * @author osboxes
 */
public class Base64Encoder {

    public static void main(String args[]) {
        Options options = new Options(args);
        options.setDefault(1, 1).setDefault(Options.Prefix.DASH);

        options.getSet().addOption(OptionData.Type.SIMPLE, "d", Options.Multiplicity.ZERO_OR_ONCE);
        options.getSet().getOption("d").setHelpText("Decode");

        options.getSet().setDataText(0, "String to encode/decode");
        options.getSet().setHelpText(0, "The string to encode or decode. Default operation is encode");

        if (options.check()) {

            String data = options.getSet().getData(0);
            if (options.getSet().getOption("d").isSet()) {
                System.out.println(new String(Base64.getDecoder().decode(data)));
            } else {
                System.out.println(Base64.getEncoder().encodeToString(data.getBytes()));
            }

        } else {
            options.printHelp("Usage:", false, true);
            System.exit(1);
        }
    }
}
