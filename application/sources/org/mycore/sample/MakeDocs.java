/*
 * $RCSfile: MCRConfiguration.java,v $
 * $Revision: 1.25 $ $Date: 2005/09/02 14:26:23 $
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRUtils;

/**
 * Builds MCR-Documents from another File containing titles
 * 
 * To use this programm do the following
 * 
 * 1. generate a docportal document template file witd id
 * "DocPortal_document_$x1" and <title xml:lang="de">$x2 </title> <title>Choose
 * http://www.mycore.de/cvs/viewcvs.cgi/content/defaultsample/document/docportal_document_00410903.xml?rev=1.3
 * and modify it. MakeDocs will replace $x1 with line number of title file and
 * $x2 with content of Line. 2. You can download a file with "titles" from
 * http://www.openthesaurus.de/download/thesaurus.txt.gz Decompress this file.
 * This file contains about 13500 lines. It contains a lot of differt german
 * words. They are well suited to test. search features of mycore. 3. Add
 * mycore-fpr-*.jar to your classpath 4. Run this programm with "titles file"
 * "template file" "output directory" 5. With the above files I got the
 * following results (Pentium 4, 2.8 GHz 512 MB Ram, XP Professional SP2, MySQL
 * and Lucene Store): - Time to generate document files: 1 minute - Time to load
 * document file with mycore.cmd 70 minutes - size of lucene index directory 11
 * mb
 * 
 * @author Harald Richter
 * @version $Revision: 1.2 $ $Date: 2005/08/15 15:01:54 $
 */
public class MakeDocs {
    public static void buildMCRDocs(String templ, File file, String dir) throws Exception {
        BufferedReader bf = new BufferedReader(new FileReader(file));
        String str;
        int i = 0;

        while ((str = bf.readLine()) != null) {
            System.out.println(i + " " + str);
            i++;
            buildSingleDoc(String.valueOf(i), new String(templ), str, dir);
        }
    }

    public static void buildSingleDoc(String id, String templ, String replace, String dir) throws Exception {
        templ = MCRUtils.replaceString(templ, "$x1", id);
        templ = MCRUtils.replaceString(templ, "$x2", replace);

        File fileo = new File(dir + "/docportal_document_" + id + ".xml");
        BufferedWriter bfw = new BufferedWriter(new FileWriter(fileo));
        bfw.write(templ);
        bfw.close();
    }

    public static void print_usage() {
        System.out.println("USE these arguments!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("arg[0] file with titles");
        System.out.println("arg[1] mcr doc template file");
        System.out.println("arg[2] directory where MCR-Docs are stored");
    }

    public static void main(String[] args) {
        MCRConfiguration.instance();

        if (args.length != 3) {
            print_usage();
        } else {
            try {
                File f = new File(args[0]);

                if (!f.exists()) {
                    throw new Exception("File with titles not found: " + args[0]);
                }

                File file = new File(args[1]);

                if (!file.exists()) {
                    throw new Exception("Template File not found: " + args[1]);
                }

                File dir = new File(args[2]);

                if (!dir.exists()) {
                    throw new Exception("Directory for results not found: " + args[2]);
                }

                if (!dir.isDirectory()) {
                    throw new Exception("Is not a directory: " + args[2]);
                }

                BufferedReader bf = new BufferedReader(new FileReader(file));
                char[] ch = new char[(int) file.length()];
                int i = bf.read(ch, 0, (int) file.length());
                System.out.println(i + " " + file.length());

                String str = new String(ch);
                System.out.println(str);

                buildMCRDocs(str, f, args[2]);
            } catch (Exception e) {
                System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
            }
        }
    }
}
