package appAsis.example.asistenciaugelcorongo.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import appAsis.example.asistenciaugelcorongo.repository.DataRepository;
import appAsis.example.asistenciaugelcorongo.utils.NetworkHelper;

public class SyncAttendanceWorker extends Worker {

    private final DataRepository repo;

    public SyncAttendanceWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
        repo = DataRepository.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Si no hay red, reintenta más tarde
        if (!NetworkHelper.isOnline(getApplicationContext())) {
            return Result.retry();
        }

        // Dispara toda la sincronización offline (data_, pdf_, img_)
        repo.syncOfflineRecords();

        return Result.success();
    }
}