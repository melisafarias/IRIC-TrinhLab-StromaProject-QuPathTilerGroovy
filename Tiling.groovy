import qupath.lib.gui.scripting.QPEx
import qupath.lib.gui.viewer.QuPathViewer
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.objects.classes.PathClassFactory
import qupath.lib.objects.PathObject
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

//-----------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------TILING---------------------------------------------------------------
//-----------------------------------------------------------------------------------------------------------------------
print("Tiling...")
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
double size_px_x = server.getWidth()
double size_px_y = server.getHeight()

// Create a new Rectangle ROI
def roi = new RectangleROI(0, 0, size_px_x, size_px_y)

// Create & new annotation & add it to the object hierarchy
//def annotation = new PathAnnotationObject(roi, PathClass.fromString("TilingRegion"))
//imageData.getHierarchy().addObject(annotation, false)
selectAnnotations()

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
//def namepart = name.split("_") // Multiplex----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY--------------------------------
//def pathOutput = buildFilePath(PROJECT_BASE_DIR, 'tiles', namepart[0] + "_" + namepart[1]) // Multiplex----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY--------------------------------
def pathOutput = buildFilePath(PROJECT_BASE_DIR, 'try', name) // H&E ----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY--------------------------------
//print name + '/' +server.getMetadata().getMagnification()
mkdirs(pathOutput)
//print name
//print pathOutput

def cal = server.getPixelCalibration()
//print (cal.getPixelHeight())

// Define output resolution in calibrated units (e.g. µm if available)
double requestedPixelSize = cal.getPixelHeight()
//print requestedPixelSize
def DesiredPixelSize = 224 // Change According to the Size of the Desired Tile
double MicronPixelSize = DesiredPixelSize*requestedPixelSize
//print(MicronPixelSize)

// tiling the image
selectAnnotations()
runPlugin('qupath.lib.algorithms.TilerPlugin','{"tileSizeMicrons":'+MicronPixelSize+',"trimToROI":true,"makeAnnotations":true,"removeParentAnnotation":true}')

def x = 0
def y = 0
def annotatedTiles = getAnnotationObjects()

for (tiles in annotatedTiles) {
    def downsample = 2 // Change Downsample Accordingly
    def tileROI = tiles.getROI()
    def area = tileROI.getArea()
    if (area == (DesiredPixelSize*DesiredPixelSize)) {
        fileName = pathOutput + "//" + name + "_x=" + x + ", y=" + y + ".tif"
        def requestROI = RegionRequest.createInstance(getCurrentServer().getPath(), downsample, tileROI)
        writeImageRegion(getCurrentServer(), requestROI, fileName)
        x = x + DesiredPixelSize
    }
    else {
        y = y + DesiredPixelSize
        x = 0
    }
}
clearAllObjects()
print("Tiling Completed!")
//-----------------------------------------------------------------------------------------------------------------------------------
//---------------------------------------------------RENAMING------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------------------------------------------------
print("Renaming...")
dh = new File(pathOutput)
//print name
index = 1
pixelImagex = 0
pixelImagey = 0
textx = "x=" + pixelImagex.toString()
texty = "y=" + pixelImagey.toString()
char segment = 'A'
def Segm = (char)segment
SegmCheck = false
index = 1
pathNames = dh.listFiles()
names = dh.list()
newName = ''
actionx = true
actiony = true
countx = 0
county = 0
//print(names.length)
i = 0
while(actionx) {
   while(actiony){ 
        for (i = 0; i < names.length; i++) {
            //print('1-'+names[i])
            //print('2-'+pathNames)
            //print(textx)
            //print(texty)
            //if (names[i].contains(textx) && names[i].contains(texty) && names[i].contains(namepart[2] + ".")) { //Multiplex----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY--------------------------------
            if (names[i].contains(textx) && names[i].contains(texty + ".")) { // H&E----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY--------------------------------
                //print(segment) 
                newName = name + '_' + segment + '_' + index + extension
                //print names[i]
                //print newName
                newName = buildFilePath(pathOutput, newName)
                pathNames[i].renameTo(newName)
                index = index + 1
                //print(index)
            }
            else {
                county = county + 1
                }
        }
        if (names.length <= county) {
            actiony = false
            countx = countx + 1
            //print('countx=' + countx)
            county = 0
            index = 1
            print('End of While-y')
        }
        else {
            // pixelImagey = pixelImagey + 4928 // Multiplex----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY////////CHANGE-ONLY-THE-VALUE--------------------------------
            pixelImagey = pixelImagey + 224 // H&E----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY////////CHANGE-ONLY-THE-VALUE--------------------------------
            county = 0
            countx = 0
            //print('county=' + county)
            texty = "y=" + pixelImagey.toString()
            //print(texty)
            //print('Continue While-y')
        }
    }
    if (countx > 1) {
        actionx = false
        ('End of While-x')
    }
    else {
        // pixelImagex = pixelImagex + 4928 // Multiplex----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY////////CHANGE-ONLY-THE-VALUE--------------------------------
        pixelImagex = pixelImagex + 224 // H&E----------------------COMMENT/UNCOMMENT-WHEN-NECESSARY////////CHANGE-ONLY-THE-VALUE--------------------------------
        pixelImagey = 0
        county = 0
        textx = "x=" + pixelImagex.toString()
        //print(textx)
        texty = "y=" + pixelImagey.toString()
        //print(texty)
        if(!SegmCheck) {
            Segm = (char)(segment + 1)
            if (segment == "[") {
                SegmCheck
            }
        }
        else {     
             segment = 'A'
            
        }
        actiony = true
        //print('Continue While-x')
    }
}
print("Renaming Completed!")
//---------------------------------------------------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------------------------------------

// Convert output resolution to a downsample factor
double pixelSize = imageData.getServer().getPixelCalibration().getAveragedPixelSize()
double downSample = requestedPixelSize / pixelSize
//print pixelSize
//print downSample


def extension = '.tif'

// Create an exporter that requests corresponding tiles from the original & labelled image servers
new TileExporter(imageData)
    .downsample(1)   // Define export resolution
    .imageExtension(extension)   // Define file extension for original pixels (often .tif, .jpg, '.png' or '.ome.tif')
    .tileSize(224)            // Define size of each tile, in pixels
    .annotatedTilesOnly(false) // If true, only export tiles if there is a (classified) annotation present
    .overlap(0)              // Define overlap, in pixel units at the export resolution
    .writeTiles(pathOutput)   // Write tiles to the specified directory

print 'done'