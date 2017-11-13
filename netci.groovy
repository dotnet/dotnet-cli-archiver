// Copyright (c) .NET Foundation and contributors. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

// Import the utility functionality.

import jobs.generation.ArchivalSettings;
import jobs.generation.Utilities;

def project = GithubProject
def branch = GithubBranchName
def isPR = true

def osList = ['OSX10.12', 'Windows_NT']
def architectureList = ['x64']
def configList ['Release']

def static getBuildJobName(def configuration, def os, def architecture) {
    return configuration.toLowerCase() + '_' + os.toLowerCase() + '_' + architecture.toLowerCase()
}

osList.each { os ->
    architectureList.each { architecture ->
        configList.each { config ->
            // Calculate job name
            def jobName = getBuildJobName(config, os, architecture)
            def buildCommand = '';

            def osBase = os
            def machineAffinity = 'latest-or-auto'

            // Calculate the build command
            if (os == 'Windows_NT') {
                buildCommand = ".\\build\\cibuild.cmd -configuration $config"
            }
            else {
                buildCommand = "./build.sh --configuration $config"
            }

            def newJob = job(Utilities.getFullJobName(project, jobName, isPR)) {
                // Set the label.
                steps {
                    if (osBase == 'Windows_NT') {
                        // Batch
                        batchFile(buildCommand)
                    }
                    else {
                        // Shell
                        shell(buildCommand)
                    }
                }
            }

            def archiveSettings = new ArchivalSettings()
            archiveSettings.addFiles("artifacts/$config/log/*")
            archiveSettings.addFiles("artifacts/$config/TestResults/*")
            archiveSettings.setFailIfNothingArchived()
            archiveSettings.setArchiveOnFailure()
            Utilities.setMachineAffinity(newJob, osBase, machineAffinity)
            Utilities.standardJobSetup(newJob, project, isPR, "*/${branch}")
            Utilities.addGithubPRTriggerForBranch(newJob, branch, "$os $architecture $config")
            Utilities.addArchival(newJob, archiveSettings)
        }
    }
}
