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
package xyz.scarabya.shuffledprojectsupdater.engine;

import java.io.File;
import xyz.scarabya.shuffledprojectsupdater.domain.SubDirNotFoundException;
import xyz.scarabya.shuffledprojectsupdater.domain.TooManyDirectoriesException;

/**
 *
 * @author Alessandro Patriarca
 */
public class Walker
{
    protected static File getSourceDir(final File projectDir,
            final String sourceDirName)
    {
        for(File file : projectDir.listFiles())
            if(file.isDirectory() && sourceDirName.equals(file.getName()))
                return file;
        return null;
    }
    
    protected static File walkInto(final File fromDir, final int hop)
            throws TooManyDirectoriesException, SubDirNotFoundException
    {
        if(hop == 0)
            return fromDir;
        else
        {
            final File[] subDirs = fromDir.listFiles();
            if(subDirs.length>0)
            {
                int i = 0, dirs=0, nextDir=0;
                while(i<subDirs.length && dirs<2)
                    if(subDirs[i].isDirectory())
                    {
                        dirs++;
                        nextDir = i;
                    }
                if(dirs == 1)
                    return walkInto(subDirs[nextDir], hop-1);
                if(dirs > 1)
                    throw new TooManyDirectoriesException();                    
            }
        }
        throw new SubDirNotFoundException();
    }
}
