import qupath.lib.gui.scripting.QPEx
import qupath.lib.gui.viewer.QuPathViewer
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.objects.classes.PathClassFactory
import qupath.lib.roi.RectangleROI
import qupath.lib.images.servers.*
import qupath.lib.images.servers.PixelCalibration
import java.awt.image.BufferedImage
import java.lang.CharSequence
import java.io.File
import javax.imageio.*
import javax.imageio.ImageIO
import qupath.lib.images.writers.ome.OMEPyramidWriter
import qupath.lib.images.servers.ImageServerMetadata

def Pass_y = true
def Pass_x = true

// Get main data structures
def imageData = QPEx.getCurrentImageData()
//print imageData
def server = imageData.getServer()

// Define starting pixel
double start_px_x = server.getWidth()/22
double start_px_y = server.getHeight()/22

// Define annotation x and y dimensions 
double size_px_x = server.getWidth()/1.12
double size_px_y = server.getHeight()/1.12

// Create a new Rectangle ROI
//def roi = new RectangleROI(start_px_x, start_px_y, size_px_x, size_px_y)

// Create & new annotation & add it to the object hierarchy
//def annotation = new PathAnnotationObject(roi, PathClassFactory.getPathClass("TilingRegion"))
//imageData.getHierarchy().addObject(annotation, false)
def annotation = selectAnnotations()

/**
 * Script to export image tiles (can be customized in various ways).
 */

// Get the current image (supports 'Run for project')

def project = getProject() 
for (entry in project.getImageList()) {
    //print entry.getImageName()
}
// Define output path (here, relative to project)
def name = GeneralTools.getNameWithoutExtension(imageData.getServer().getMetadata().getName())
//def namepart = name.split("_") // Multiplex
//def pathOutput = buildFilePath(PROJECT_BASE_DIR, 'tiles', namepart[0] + "_" + namepart[1]) // Mutlttiplex
def pathOutput = buildFilePath(PROJECT_BASE_DIR, 'tiles', name) // H&E
print name + '/' +server.getMetadata().getMagnification()
mkdirs(pathOutput)
//print name
//print pathOutput


// tiling the image
//runPlugin('qupath.lib.algorithms.TilerPlugin', '{"tileSizeMicrons":123.077,"trimToROI":true,"makeAnnotations":false,"removeParentAnnotation":false}')

def cal = server.getPixelCalibration()
//print (cal.getPixelHeight())

// Define output resolution in calibrated units (e.g. µm if available)
double requestedPixelSize = cal.getPixelHeight()
//print requestedPixelSize

// Convert output resolution to a downsample factor
double pixelSize = imageData.getServer().getPixelCalibration().getAveragedPixelSize()
double downSample = requestedPixelSize / pixelSize
//print pixelSize
//print downSample

def extension = '.ome.tif'

//----------------------------------------------------------------------------------------------------------------------
dh = new File(pathOutput)
print name
index = 1
pixelImagex = 0
pixelImagey = 0
textx = "x=" + pixelImagex.toString()
texty = "y=" + pixelImagey.toString()
char segment = 'A'
index = 1
pathNames = dh.listFiles()
names = dh.list()
newName = ''
actionx = true
actiony = true
countx = 0
county = 0
print(names.length)
i = 0
while(actionx) {
   while(actiony){ 
        for (i = 0; i < names.length; i++) {
            //print('1-'+names[i])
            //print('2-'+pathNames)
            //print(textx)
            //print(texty)
            //if (names[i].contains(textx) && names[i].contains(texty) && names[i].contains(namepart[2] + ".")) { //Multiplex
            if (names[i].contains(textx) && names[i].contains(texty + ".")) { // H&E
                print(segment) 
                newName = name + '_' + segment + '_' + index + extension
                print names[i]
                print newName
                newName = buildFilePath(pathOutput, newName)
                pathNames[i].renameTo(newName)
                index = index + 1
                print(index)
            }
            else {
                county = county + 1
                }
        }
        if (names.length <= county) {
            actiony = false
            countx = countx + 1
            print('countx=' + countx)
            county = 0
            index = 1
            print('End of While-y')
        }
        else {
            // pixelImagey = pixelImagey + 4928 // Multiplex
            pixelImagey = pixelImagey + 224 // H&E
            county = 0
            countx = 0
            print('county=' + county)
            texty = "y=" + pixelImagey.toString()
            print(texty)
            print('Continue While-y')
        }
    }
    if (countx > 1) {
        actionx = false
        ('End of While-x')
    }
    else {
        // pixelImagex = pixelImagex + 4928 // Multiplex
        pixelImagex = pixelImagex + 224 // H&E
        pixelImagey = 0
        county = 0
        textx = "x=" + pixelImagex.toString()
        print(textx)
        texty = "y=" + pixelImagey.toString()
        print(texty)
        segment = (char)(segment + 1)
        if(segment == "[") {
            char segment = 'A'
            
        }
       
        segment = (char)(segment + 1)
        actiony = true
        print('Continue While-x')
    }
}