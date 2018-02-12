/*
 * Copyright 2018 Alessandro Patriarca.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.scarabya.shuffledprojectsupdater;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import xyz.scarabya.shuffledprojectsupdater.domain.Operation;
import xyz.scarabya.shuffledprojectsupdater.engine.Engine;
import xyz.scarabya.shuffledprojectsupdater.log.LightLogger;

/**
 *
 * @author Alessandro Patriarca
 */
public class Main
{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) throws Exception
    {
        LightLogger.setup();
    
        File originalFolder = showFileChooser(
                "Seleziona la cartella del parent project aggiornato", null);
        File folderToUpdate = showFileChooser(
                "Seleziona la cartella del parent da aggiornare", null);
        
        String sourceRootName = JOptionPane.showInputDialog(
                "Inserisci il nome della cartella dei sorgenti (es. \"src\")",
                "src");
        int sottocartelle = Integer.parseInt(JOptionPane.showInputDialog(
                "Inserisci il numero di sottocartelle da saltare",
                "0"));
        
        Operation operation = showOperationChooser();
        
        Engine engine = new Engine();
        
        engine.doOperation(originalFolder, sourceRootName, sottocartelle,
                Operation.CREATE);        
        engine.doOperation(folderToUpdate, sourceRootName, sottocartelle,
                operation);   
    }
    
    private static File showFileChooser(String message, String extension)
    {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView()
                .getHomeDirectory());
        jfc.setDialogTitle(message);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.showOpenDialog(null);
        return jfc.getSelectedFile();
    }
    
    private static Operation showOperationChooser()
    {
        String[] options = {"Verifica file", "Aggiorna file"};
        Operation[] operations = {Operation.CHECK, Operation.UPDATE};
        return operations[JOptionPane.showOptionDialog(null, "Cosa vuoi fare?",
                "Scelta operazione",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0])];
    }
}
