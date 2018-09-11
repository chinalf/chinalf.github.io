import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.json.StringEscapeUtils

import java.security.MessageDigest
import java.util.zip.ZipFile

import static groovy.io.FileType.ANY

public class UpgradeTool {


    public static final String PROGRAM = "program"
    public static String url = "http://172.17.189.71/itone/repos/";

    def deleteZips(String zips) {
        println "delete files:" + zips
        zips.split(',').each { it -> delete it;}
	println "delete files ok!"
    }

    def downloadZips(String zips) {
        println "download files:" + zips
        String[] zipNames = zips.split(",")
        StringBuilder sb = new StringBuilder();
        zipNames.each { String zipName ->
            println "download " + zipName
            download(url + zipName, zipName)
            sb << zipName + ":" + md5(new File(zipName)) + ","
        }
        println "download files ok!"
        return sb.toString()
    }

    def download(def remoteUrl, def localUrl) {
        def File file = new File("$localUrl")
        file.withOutputStream { out ->
            new URL(remoteUrl).withInputStream { from -> out << from; }
        }
    }

    def copy(String from, String to) {
        File fromFile = new File(from)
        if (!fromFile.exists()) {
            return
        }
        if (fromFile.isDirectory()) {
            File toFile = new File(to)
            if (!toFile.exists()) {
                toFile.mkdirs()
            }
            for (File child : fromFile.listFiles()) {
                String childFrom = from + File.separator + child.getName()
                String childTo = to + File.separator + child.getName()
                copy(childFrom, childTo)
            }
        } else {
            new File(to).withOutputStream {
                out -> fromFile.withInputStream { input -> out << input }
            }
        }
    }

    def delete(String fileName, def match = null) {
        File file = new File(fileName)
        if (!file.exists()) {
            return
        } else if (match == null) {
            boolean flag = file.isDirectory() ? file.deleteDir() : file.delete()
            println "DELTE FILE " + fileName + ":" + flag
        } else {
            file.eachFileMatch ANY, match, {
                boolean flag = it.isDirectory() ? it.deleteDir() : it.delete()
                println "DELTE FILE " + it.getName() + ":" + flag
            }
        }
    }

    def stopAndClean() {
        delete("bak")
        copy("${PROGRAM}/work", "bak/work")
        copy("${PROGRAM}/logs", "bak/logs")
        copy("workspace","bak/workspace")
        delete(PROGRAM)
    }

    def unzip(String zips) {
        println "unzip files:" + zips
        String[] zipNames = zips.split(",")
        zipNames.each { String zipName -> unzipFile(zipName)}
	println "unizp files ok!"
    }

    def unzipFile(String zipName){
        Process process = null;
        InputStreamReader ir = null;
        BufferedReader input = null;
        try {
            String command = "unzip -o " + zipName + " -d ${PROGRAM}/";
            process = Runtime.getRuntime().exec(command);
            ir = new InputStreamReader(process.getInputStream());
            input = new BufferedReader(ir);
            String line;
            while ((line = input.readLine()) != null) {
            }
            println "unzip " + zipName + " ok!"
        } catch (Throwable e) {
            println "unzip " + zipName + " Error!"
        } finally {
            try {
                if (input != null) {
                    input.close();
                    input.close();
                    input.close();
                }
                if (ir != null) {
                    ir.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Throwable t) {
                println "Error!!!"
            }
        }
   }


   def findJarFile(def zipFile, String target) {
        def ret = [:]
        ZipFile zip = new ZipFile(zipFile)
        zip.withCloseable
                {
                    def entry = zip.getEntry(target)
                    if (!entry) {
                        return
                    }
                    def en = zip.getInputStream(entry)
                    ret = JsonSlurper.newInstance().parse(en, 'utf-8')
                }
        return ret
    }

    def mergeJSONConfig(def files, String target) {
        def result = [:]
        files.each {
            findJarFile(it, target).each { k, v ->
                result.putAt(k, v instanceof List ? result.getOrDefault(k, []) + v : v)
            }
        }
	def daemons = result.get("daemons");
	daemons.remove("com.its.itone.DbPatcherDaemon"); // Stop DbPatcherDaemon
	result.put("daemons",daemons)
        println "restult: " + result
        println "--------------End-------------------"
        return StringEscapeUtils.unescapeJava(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
    }

    def doMergeConfig(String zips) {
        println "--------------Beigin In order to merge files---------------"
        println "zips: " +  zips
        def files = zips.split(",").collect { new File(it) }
        println "files: " + files
        def file = new File("./${PROGRAM}/conf/its_config.json")
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
            file.createNewFile();
        }
        file.write(mergeJSONConfig(files, 'conf/its_config.json'), "utf-8")
    }

    def doApplayCutomization(String apps) {
        copy("bak/conf/plugin","${PROGRAM}/conf/plugin")
        copy("${PROGRAM}/workspace", "workspace")
	copy("bak/workspace","workspace")
	copy("${PROGRAM}/work" + File.separator + apps, PROGRAM)
        delete "${PROGRAM}/work", ~/.*/
    }

    def addJar(String path) {
                File file = new File(path);
                if (!file.exists()) {
                        file.mkdirs();
                }
        file.eachFileRecurse {
            if (it.name ==~ /.*jar/) {
                this.class.classLoader.rootLoader.addURL(new URL("file:" + it));
            }
        }
    }

    def doMakeLicense() {
        addJar("${PROGRAM}/lib/3rd/")
        addJar("${PROGRAM}/lib/app/")
        def licenseService = Class.forName("com.its.itone.license.LicenseService").newInstance();
        licenseService.buildDefaultLicense("${PROGRAM}/ione.lic")
    }

    def md5(obj) {
        def hash = MessageDigest.getInstance('MD5').with {
            if (obj instanceof byte[]) {
                update obj
            } else {
                obj.eachByte(8192) { bfr, num -> update bfr, 0, num }
            }
            it.digest()
        }
        new BigInteger(1, hash).toString(16).padLeft(32, '0')
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            println "usage: UpgradeTool zipNames appName serviceName"
            println "       zipNames With the comma-separated product compressed file name, For example: itone.zip itone_alarm.zip"
            System.exit(1)
        }
	String VERSION = args.length == 3 ? args[2] : "1.5.4"
	String HISTROY = args.length == 4 ? "histroy" + VERSION + "/" + args[3] : VERSION
        url = url + HISTROY + "/"
        println "URL:" + url
        System.setOut(new PrintStream(System.out, true, 'gbk'))
        def appZipNames = args[0]
        new UpgradeTool().with {
            deleteZips(appZipNames)
            def version_detail = downloadZips(appZipNames)
            stopAndClean()
            unzip(appZipNames)
            doMergeConfig(appZipNames)
            doMakeLicense()
            deleteZips(appZipNames)
            doApplayCutomization(args[1])
            println "The module version:\n" + version_detail.replaceAll(',', '\n')
            println "Application version:" + md5(version_detail.getBytes())
        }
    }
}
