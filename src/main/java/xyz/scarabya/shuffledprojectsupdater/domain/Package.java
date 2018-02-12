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
package xyz.scarabya.shuffledprojectsupdater.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Alessandro Patriarca
 */
public class Package
{
    private final Map<String, OriginalFile> files;
    private final Map<String, Package> subPackages;

    public Package()
    {
        files = new HashMap<>();
        subPackages = new HashMap<>();
    }
    
    public void mergeUsingPkg(final Package usingPkg)
            throws DuplicateFileFoundException
    {
        for(String fileName : usingPkg.getFileNames())
        {
            if(!files.containsKey(fileName))
                files.put(fileName, usingPkg.getOriginalFile(fileName));
            else
                throw new DuplicateFileFoundException();
        }
        
        for(String pkgName : usingPkg.getPackagesNames())
        {
            if(!subPackages.containsKey(pkgName))
                subPackages.put(pkgName, usingPkg.getSubPackage(pkgName));
            else
                subPackages.get(pkgName)
                        .mergeUsingPkg(usingPkg.getSubPackage(pkgName));
        }
    }
    
    public void addFile(final String fileName, final OriginalFile file)
    {
        files.put(fileName, file);
    }
    
    public void addPackage(String packageName, Package newPackage)
    {
        subPackages.put(packageName, newPackage);
    }
    
    public Package getSubPackage(final String subPackageName)
    {
        return subPackages.get(subPackageName);
    }
    
    public String getProjectNameByFile(final String fileName)
    {
        return files.get(fileName).getProjectName();
    }
    
    public OriginalFile getOriginalFile(final String fileName)
    {
        return files.get(fileName);
    }    
    
    protected Set<String> getFileNames()
    {
        return files.keySet();
    }
    
    protected Set<String> getPackagesNames()
    {
        return subPackages.keySet();
    }
}
