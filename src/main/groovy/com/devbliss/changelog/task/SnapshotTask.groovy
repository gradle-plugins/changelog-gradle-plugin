package com.devbliss.changelog.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * This task defines the changelogSnapshot. It differentiates whether a snapshot version already exists.
 * In case of an already existing snapshot version, only a new entry tab will be popped
 * onto the first position of the current snapshot and the actual timestamp will be replaced in snapshot name.
 * In case there is no snapshot version in the changelog file, there will be the latest release number
 * increased at the minor level to generate a completely new snapshot version paragraph within a new
 * entry tab depending on the branch type (e.g. feature, bug, refactor etc.) and the given user description.
 *
 * @author Christian Soth <christian.soth@devbliss.com>
 * @author Philipp Karstedt <philipp.karstedt@devbliss.com>
 *
 * @version 0.0.1
 */

class SnapshotTask extends ChangelogTask{

  @TaskAction
  public void run() {
    super.run()
    def snapshotVersion

    println "Add Snapshot to "+ getFilename()
    def changelogToString = changelogFile.text
    def versionLine = changelogToString.find(Utility.regexVersionWithoutSuffix)
    def isAlreadySnapshotVersion = versionLine.contains("-SNAPSHOT-")
    def versionNumber = versionLine.find(Utility.regexVersionNumber)

    // handle version line and increment version number for a new snapshot version
    if (isAlreadySnapshotVersion) {
      snapshotVersion = versionLine.replaceFirst(versionLine, "$versionNumber-SNAPSHOT-" + today.time)
    } else {
      // extract minor version number in order to increment it.
      // only works for the version number layout we use at the moment: e.g. --> 0.0.0
      def major = versionNumber.subSequence(0, 1)
      def minor = versionNumber.subSequence(2, 3) as int
      minor++
      def incrementedVersionNumber = major + "." + minor + ".0"

      snapshotVersion = versionLine.replaceFirst(versionLine, "$incrementedVersionNumber-SNAPSHOT-" + today.time)
    }

    println Utility.RED + " New snapshot version created" + Utility.RED_BOLD + " $snapshotVersion"

    def change = System.console().readLine Utility.RED + " Change:" + Utility.WHITE + " [$branch] "

    def temp = changelogFile.text
    if (isAlreadySnapshotVersion) {
      temp = temp.replaceFirst(Utility.regexVersionWithSuffix, snapshotVersion)
      def oldChanges = temp.find(Utility.regexText)
      def newstuff = " - [$branch] " + change + Utility.NEWLINE + oldChanges
      temp = temp.replaceFirst(Utility.regexText, newstuff)
      temp = temp.replaceFirst(Utility.regexChangeNameDate, Information.getChangeFrom(today))
      changelogFile.delete()
      changelogFile = new File(getFilename())
      changelogFile << temp
    } else {
      changelogFile.delete()
      changelogFile = new File(getFilename())
      changelogFile << Utility.NEWLINE + "### Version $snapshotVersion" + Utility.NEWLINE
      changelogFile << " - [$branch] " + change + Utility.NEWLINE
      changelogFile << Utility.NEWLINE + Information.getChangeFrom(today) + Utility.NEWLINE + Utility.NEWLINE
      changelogFile << temp
    }
  }
}
