#!/usr/bin/env bash

set -e

rm -rf build

./gradlew -q clean check install --stacktrace

./integration-test-app/run_integration_tests.sh

if [[ -n $TRAVIS_TAG && $TRAVIS_BRANCH == 'master' && $TRAVIS_PULL_REQUEST == 'false' ]]; then

	./gradlew bintrayUpload --stacktrace

    ./build-docs.sh

	git config --global user.name "$GIT_NAME"
	git config --global user.email "$GIT_EMAIL"
	git config --global credential.helper "store --file=~/.git-credentials"
	echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

	git checkout gh-pages

	git rm v3/jms-*.epub
	mv build/docs/jms-*.epub v3
	git add v3/jms-*.epub

	git rm v3/jms-*.pdf
	mv build/docs/jms-*.pdf v3
	git add v3/jms-*.pdf

	mv build/docs/index.html v3
	git add v3/index.html

	mv build/docs/ghpages.html index.html
	git add index.html

	git commit -a -m "Updating docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
	git push origin

fi