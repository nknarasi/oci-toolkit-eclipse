package com.oracle.oci.eclipse.ui.account;

import java.util.List;
import com.oracle.bmc.objectstorage.model.Bucket;
import com.oracle.bmc.objectstorage.model.BucketSummary;
import com.oracle.bmc.objectstorage.model.ObjectSummary;

public class BucketNode {

	private final List<ObjectSummary> bucketcontents;
	private final BucketSummary bucket;

	public BucketNode(BucketSummary node, List<ObjectSummary> objects) {
		this.bucket = node;
		this.bucketcontents = objects;
	}

	public BucketSummary getBucket() {
		return bucket;
	}

	public List<ObjectSummary> getBucketContents() {
		return bucketcontents;
	}
}
