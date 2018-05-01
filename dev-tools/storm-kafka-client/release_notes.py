#!/usr/bin/env python

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Usage: release_notes.py <version> > RELEASE_NOTES.html

Depends on https://pypi.python.org/pypi/jira/, please use pip to install this module.

Generates release notes for a Storm-kafka-client release by generating an HTML doc containing some introductory information about the
 release with links to the Storm docs followed by a list of issues resolved in the release. The script will fail if it finds
 any unresolved issues still marked with the target release. You should run this script after either resolving all issues or
 moving outstanding issues to a later release.

"""

from jira import JIRA
import itertools, sys

if len(sys.argv) < 2:
    print >>sys.stderr, "Usage: release_notes.py <version>"
    sys.exit(1)

version = sys.argv[1]

JIRA_BASE_URL = 'https://issues.apache.org/jira'
MAX_RESULTS = 100 # This is constrained for cloud instances so we need to fix this value

def get_issues(jira, query, **kwargs):
    """
    Get all issues matching the JQL query from the JIRA instance. This handles expanding paginated results for you. Any additional keyword arguments are forwarded to the JIRA.search_issues call.
    """
    results = []
    startAt = 0
    new_results = None
    while new_results == None or len(new_results) == MAX_RESULTS:
        new_results = jira.search_issues(query, startAt=startAt, maxResults=MAX_RESULTS, **kwargs)
        results += new_results
        startAt += len(new_results)
    return results

def issue_link(issue):
    return "%s/browse/%s" % (JIRA_BASE_URL, issue.key)


if __name__ == "__main__":
    apache = JIRA(JIRA_BASE_URL)
    issues = get_issues(apache, 'project=STORM and fixVersion=%s and component=storm-kafka-client' % version)
    if not issues:
        print >>sys.stderr, "Didn't find any issues for the target fix version"
        sys.exit(1)

    # Some resolutions, including a lack of resolution, indicate that the bug hasn't actually been addressed and we shouldn't even be able to create a release until they are fixed
    UNRESOLVED_RESOLUTIONS = [None,
                              "Unresolved",
                              "Duplicate",
                              "Invalid",
                              "Not A Problem",
                              "Not A Bug",
                              "Won't Fix",
                              "Incomplete",
                              "Cannot Reproduce",
                              "Later",
                              "Works for Me",
                              "Workaround",
                              "Information Provided"
                              ]
    unresolved_issues = [issue for issue in issues if issue.fields.resolution in UNRESOLVED_RESOLUTIONS or issue.fields.resolution.name in UNRESOLVED_RESOLUTIONS]
    if unresolved_issues:
        print >>sys.stderr, "The release is not completed since unresolved issues or improperly resolved issues were found still tagged with this release as the fix version:"
        for issue in unresolved_issues:
            print >>sys.stderr, "Unresolved issue: %15s %20s %s" % (issue.key, issue.fields.resolution, issue_link(issue))
        print >>sys.stderr
        print >>sys.stderr, "Note that for some resolutions, you should simply remove the fix version as they have not been truly fixed in this release."
        sys.exit(1)

    # Get list of (issue type, [issues]) sorted by the issue ID type, with each subset of issues sorted by their key so they
    # are in increasing order of bug #. To get a nice ordering of the issue types we customize the key used to sort by issue
    # type a bit to ensure features and improvements end up first.
    def issue_type_key(issue):
        if issue.fields.issuetype.name == 'New Feature':
            return -2
        if issue.fields.issuetype.name == 'Improvement':
            return -1
        return issue.fields.issuetype.id
    by_group = [(k,sorted(g, key=lambda issue: issue.id)) for k,g in itertools.groupby(sorted(issues, key=issue_type_key), lambda issue: issue.fields.issuetype.name)]

    print "<!DOCTYPE html>"
    print "<html lang=\"en\">"
    print "<head>"
    print "<meta charset=\"utf-8\">"
    print "<title>Storm-kafka-client %(version)s Release Notes</title>" % { 'version': version }
    print "</head>"
    print "<body>"
    print "<h1>Release Notes for Storm-kafka-client %s</h1>" % version
    print """<p>JIRA issues addressed in the %(version)s release of Storm-kafka-client. Documentation for this
    release is available at the <a href="http://storm.apache.org/">Apache Storm
    project site</a>.</p>""" % { 'version': version }
    for itype, issues in by_group:
        print "<h2>%s</h2>" % itype
        print "<ul>"
        for issue in issues:
            print '<li>[<a href="%(link)s">%(key)s</a>] - %(summary)s</li>' % {'key': issue.key, 'link': issue_link(issue), 'summary': issue.fields.summary}
        print "</ul>"
    print "</body>"
    print "</html>"
