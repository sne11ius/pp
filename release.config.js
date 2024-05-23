const releaseConfig = {
  branches: ['main'],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
    [
      "@semantic-release/changelog",
      {
        "changelogFile": "CHANGELOG.md"
      }
    ],
    [
      "semantic-release-replace-plugin",
      {
        "replacements": [
          {
            "files": ["client/root.go"],
            "from": "var version = \".*\"",
            "to": "var version = \"${nextRelease.version}\"",
            "results": [
              {
                "file": "client/root.go",
                "hasChanged": true,
                "numMatches": 1,
                "numReplacements": 1
              }
            ],
            "countMatches": true
          }, {
            "files": ["api/build.gradle.kts"],
            "from": "version = \".*\"",
            "to": "version = \"${nextRelease.version}\"",
            "results": [
              {
                "file": "client/root.go",
                "hasChanged": true,
                "numMatches": 1,
                "numReplacements": 1
              }
            ],
            "countMatches": true
          }
        ]
      }
    ],
    ['@semantic-release/git', {
      'assets': [
        'client/root.go',
        'api/build.gradle.kts',
        'CHANGELOG.md'
      ],
      'message': 'chore(release): ${nextRelease.version} [skip ci]\n\n${nextRelease.notes}'
    }],
    ["@semantic-release/exec", {
      "verifyConditionsCmd": "./verify.sh",
      "publishCmd": "./publish.sh ${nextRelease.version}"
    }],
    '@semantic-release/github'
  ]
}

module.exports = releaseConfig
