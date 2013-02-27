#MOLGENIS
# description This protocol will
# description copy files from a source directory to a group directory
# string sourceFile "this is the original path"
# output targetFile CopyOf${sourceFile} "this is where we will copy to"

cp ${sourceFile} ${targetFile}