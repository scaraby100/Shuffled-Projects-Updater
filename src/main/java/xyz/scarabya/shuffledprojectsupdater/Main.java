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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
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
    
    private final static File DIR_EXCLUSION_FILE = new File("dir.exclusion");
    private final static File FILE_EXCLUSION_FILE = new File("file.exclusion");

    public static void main(String[] args) throws Exception
    {
        LightLogger.setup();
    
        File originalFolder = showFileChooser(
                "Seleziona la cartella del parent project aggiornato");
        File folderToUpdate = showFileChooser(
                "Seleziona la cartella del parent da aggiornare");
        
        String sourceRootName = JOptionPane.showInputDialog(
                "Inserisci il nome della cartella dei sorgenti (es. \"src\")",
                "src");
        int sottocartelle = Integer.parseInt(JOptionPane.showInputDialog(
                "Inserisci il numero di sottocartelle da saltare",
                "0"));
        
        JOptionPane.showMessageDialog(null, "Puoi definire una lista di "
                + "esclusioni (un valore per riga) nei file: \ndir.exclusion "
                + "(esclusione directory progetti)\nfile.exclusion "
                + "(esclusione file)",
                "Esclusioni", JOptionPane.INFORMATION_MESSAGE);
        
        final Set<String> dirToExclude = new HashSet<>();
        final Set<String> fileToExclude = new HashSet<>();
        
        String line;                
        if(DIR_EXCLUSION_FILE.exists())
        {
            try (BufferedReader br = new BufferedReader(
                    new FileReader(DIR_EXCLUSION_FILE)))
            {
                while ((line = br.readLine()) != null)
                    dirToExclude.add(line);
            }
        }
        if(FILE_EXCLUSION_FILE.exists())
        {
            try (BufferedReader br = new BufferedReader(
                    new FileReader(FILE_EXCLUSION_FILE)))
            {
                while ((line = br.readLine()) != null)
                    fileToExclude.add(line);
            }
        }
        
        Operation operation = showOperationChooser();
        
        Engine engine = new Engine(dirToExclude, fileToExclude);
        
        engine.doOperation(originalFolder, sourceRootName, sottocartelle,
                Operation.CREATE);        
        engine.doOperation(folderToUpdate, sourceRootName, sottocartelle,
                operation);   
    }
    
    private static File showFileChooser(String message)
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
