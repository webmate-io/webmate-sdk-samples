#!/bin/bash

project_id=$1
path_to_app=$2
username=$3
apikey=$4
name=$5

suffix="${path_to_app##*.}"

if [ x$suffix = x"apk" ]; then
  content_type="application/vnd.android.package-archive"
elif [ x$suffix = x"ipa" ]; then
  content_type="application/x-ios-app"
fi

blob_result=$(curl -H "Content-Type: ${content_type}" -H "webmate.user: ${username}" -H "webmate.api-token: ${apikey}" --data-binary "@${path_to_app}" https://app.webmate.io/api/v1/projects/${project_id}/blobs)

package_content="{ \"name\": \"${name}\", \"blobId\": ${blob_result}, \"extension\": \"$suffix\" }"

package_id=$(curl -H "Content-Type: application/json" -d "${package_content}" -H "webmate.user: ${username}" -H "webmate.api-token: ${apikey}" https://app.webmate.io/api/v1/projects/${project_id}/packages)

if [ $? = 0 ]; then
  echo Package ${path_to_app} of type ${suffix} successfully added to project ${project_id} with id ${package_id}
else
  echo Package upload failed
fi
