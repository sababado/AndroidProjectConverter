package com.sababado.android.converter.network;

import com.sababado.android.converter.models.ChangeLog;

import retrofit.http.GET;

/**
 * Created by Robert on 8/25/13.
 */
public interface ChangeLogService {
    @GET("/changelog.json")
    public ChangeLog getChangeLog();
}
