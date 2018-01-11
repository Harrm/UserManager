var gulp = require('gulp');


gulp.task('default', defaultTask);

function defaultTask(done) {
    gulp.src('build/**')
        .pipe(gulp.dest('../../../jetty_base/webapps/ROOT'));

    done();
}
