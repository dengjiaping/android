package com.oumen.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.oumen.book.BookMessage;
import com.oumen.circle.SimpleCircle;
import com.oumen.friend.Friend;
import com.oumen.message.ActivityMessage;
import com.oumen.message.BaseMessage;
import com.oumen.message.ChatMessage;
import com.oumen.message.CircleMessage;
import com.oumen.message.FriendMessage;
import com.oumen.message.HelpMessage;
import com.oumen.message.MultiChatMessage;
import com.oumen.mv.MvInfo;
import com.oumen.mv.PrefixVideo;
import com.oumen.mv.index.UploadTask;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "oumen_db";
	private static final int version = 1;

	public static final String KEY_ID = "id";
	public static final String KEY_SELF_UID = "self_uid";
	
	public static final String TABLE_PINYIN = "py";

	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(ChatMessage.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(ChatMessage.KEY_SELF_NAME).append("` VARCHAR,")
			.append("`").append(ChatMessage.KEY_SELF_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_TARGET_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_NAME).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_TARGET_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_CONTENT).append("` VARCHAR NOT NULL,")
			.append("`").append(BaseMessage.KEY_DATETIME).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_ACTION_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(ChatMessage.KEY_SEND).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_READ).append("` INTEGER NOT NULL DEFAULT 0)");
//			.append(" (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `type` INTEGER NOT NULL DEFAULT 0, `self_uid` INTEGER NOT NULL, `target_uid` INTEGER NOT NULL, `target_nick` VARCHAR, `target_photo_url` VARCHAR, `anim` INTEGER, `datetime` INTEGER NOT NULL, `is_come` INTEGER NOT NULL, `is_read` INTEGER NOT NULL DEFAULT 0, `content` VARCHAR)");
		db.execSQL(sql.toString());

		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(FriendMessage.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_ACTION_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_NAME).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_TARGET_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_CONTENT).append("` VARCHAR NOT NULL,")
			.append("`").append(BaseMessage.KEY_DATETIME).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_READ).append("` INTEGER NOT NULL DEFAULT 0)");
//			.append(" (`self_uid` INTEGER NOT NULL, `type` INTEGER NOT NULL, `target_uid` INTEGER NOT NULL, `target_nick` VARCHAR NOT NULL, `target_photo_url` VARCHAR, `datetime` INTEGER NOT NULL, `content` VARCHAR,`unread` INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());

		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(Friend.TABLE)
			.append(" (self_uid INTEGER NOT NULL, uid INTEGER NOT NULL, nickname VARCHAR NOT NULL, description VARCHAR, photo_url VARCHAR, baby_type INTEGER NOT NULL DEFAULT 2, gravidity VARCHAR, birthday VARCHAR, gender INTEGER NOT NULL DEFAULT 0, address VARCHAR, number INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());

		sql = new StringBuilder().append("CREATE table IF NOT EXISTS ").append(TABLE_PINYIN).append(" (`unicode` INTEGER PRIMARY KEY, py VARCHAR NOT NULL)");
		db.execSQL(sql.toString());

//		sql = new StringBuilder()
//			.append("CREATE table IF NOT EXISTS ")
//			.append(TABLE_OUMEN_CIRCLE_NOTICE)
//			.append("(`id` INTEGER PRIMARY KEY AUTOINCREMENT, `cid` INTEGER NOT NULL DEFAULT 0, `title` VARCHAR, `picUrl` VARCHAR, `fromId` INTEGER NOT NULL DEFAULT 0, `fromNickName` VARCHAR, `fromHeadPhoto` VARCHAR, "
//						+ "`targetId` INTEGER NOT NULL DEFAULT 0, `action_type` VARCHAR NOT NULL, `type` INTEGER NOT NULL DEFAULT 1, `timetemp` INTEGER NOT NULL," + "`commentId` INTEGER NOT NULL DEFAULT 0,`commentContent` VARCHAR,`priseId` INTEGER NOT NULL DEFAULT 0,`unread` INTEGER NOT NULL DEFAULT 0)");
//		db.execSQL(sql.toString());

		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(CircleMessage.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(CircleMessage.KEY_CIRCLE_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(CircleMessage.KEY_CIRCLE_TITLE).append("` VARCHAR,")
			.append("`").append(CircleMessage.KEY_CIRCLE_PIC).append("` VARCHAR,")
			.append("`").append(CircleMessage.KEY_ABOUT_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_NAME).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_TARGET_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_CONTENT).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_DATETIME).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_ACTION_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_READ).append("` INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());
		
		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(PrefixVideo.TABLE)
			.append("(`" + PrefixVideo.KEY_ID + "` INTEGER PRIMARY KEY,`" + PrefixVideo.KEY_NAME + "` VARCHAR NOT NULL,`")
			.append(PrefixVideo.KEY_TOTAL).append("` INTEGER NOT NULL DEFAULT 0,`")
			.append(PrefixVideo.KEY_TOTAL_DESCRIPTION).append("` VARCHAR, `")
			.append(PrefixVideo.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,`")
			.append(PrefixVideo.KEY_TYPE_TITLE).append("` VARCHAR,`")
			.append(PrefixVideo.KEY_DESCRIPTION).append("` VARCHAR,`")
			.append(PrefixVideo.KEY_CREARE_AT).append("` VARCHAR)");
		db.execSQL(sql.toString());
		
		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(MvInfo.TABLE)
			.append("(`").append(MvInfo.KEY_USER_ID).append("` INTEGER NOT NULL DEFAULT 0, `")
			.append(MvInfo.KEY_TITLE).append("` VARCHAR PRIMARY KEY,`")
			.append(MvInfo.KEY_PREFIX_ID).append("` INTEGER NOT NULL DEFAULT 0,`")
			.append(MvInfo.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,`")
			.append(MvInfo.KEY_DATE).append("` INTEGER NOT NULL DEFAULT 0, `")
			.append(MvInfo.KEY_SERVER_ID).append("` INTEGER NOT NULL DEFAULT 0, `")
			.append(MvInfo.KEY_SERVER_URL).append("` VARCHAR)");
		db.execSQL(sql.toString());
		
		//---------- 1.0.3 -----------//
		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(ActivityMessage.TABLE)
			.append("(`").append(ActivityMessage.KEY_ID).append("` INTEGER NOT NULL DEFAULT -1,")
			.append("`").append(ActivityMessage.KEY_TITLE).append("` VARCHAR NOT NULL,")
			.append("`").append(ActivityMessage.KEY_DESCRIPTION).append("` VARCHAR NOT NULL,")
			.append("`").append(ActivityMessage.KEY_ADDRESS).append("` VARCHAR,")
			.append("`").append(ActivityMessage.KEY_START).append("` INTEGER NOT NULL DEFAULT -1,")
			.append("`").append(ActivityMessage.KEY_PIC_URL).append("` VARCHAR NOT NULL,")
			.append("`").append(ActivityMessage.KEY_OWNER_ID).append("` INTEGER NOT NULL DEFAULT -1,")
			.append("`").append(ActivityMessage.KEY_OWNER_NAME).append("` VARCHAR,")
			.append("`").append(ActivityMessage.KEY_OWNER_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(ActivityMessage.KEY_TIMESTAMP).append("` INTEGER NOT NULL DEFAULT -1,")
			.append("`").append(ActivityMessage.KEY_SCOPE).append("` INTEGER NOT NULL DEFAULT -1,")
			.append("`").append(ActivityMessage.KEY_LOOKNUM).append("` VARCHAR,")
			.append("`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT -1,")
			.append("`").append(ActivityMessage.KEY_PUSH).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_READ).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(ActivityMessage.KEY_DELETE).append("` INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());
		
		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(MultiChatMessage.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(ChatMessage.KEY_SELF_NAME).append("` VARCHAR,")
			.append("`").append(ChatMessage.KEY_SELF_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_TARGET_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_NAME).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_TARGET_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_CONTENT).append("` VARCHAR NOT NULL,")
			.append("`").append(BaseMessage.KEY_DATETIME).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_ACTION_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(ChatMessage.KEY_SEND).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_READ).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(MultiChatMessage.KEY_ACTIVITY_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(MultiChatMessage.KEY_MULTI_ID).append("` INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());
		
		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(HelpMessage.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(HelpMessage.KEY_GROUP_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TARGET_NAME).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_TARGET_PHOTO_URL).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_DATETIME).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_ACTION_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_READ).append("` INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());

		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(BookMessage.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(KEY_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BookMessage.KEY_BABY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BookMessage.KEY_TITLE).append("` VARCHAR,")
			.append("`").append(BookMessage.KEY_DAYS).append("` VARCHAR,")
			.append("`").append(BookMessage.KEY_URL).append("` VARCHAR,")
			.append("`").append(BookMessage.KEY_CREATE_AT).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_CONTENT).append("` VARCHAR,")
			.append("`").append(BaseMessage.KEY_DATETIME).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_ACTION_TYPE).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(BaseMessage.KEY_READ).append("` INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());

		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(SimpleCircle.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(KEY_ID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(SimpleCircle.KEY_JSON).append("` TEXT NOT NULL)");
		db.execSQL(sql.toString());
		
		sql = new StringBuilder()
			.append("CREATE table IF NOT EXISTS ")
			.append(UploadTask.TABLE)
			.append("(`").append(KEY_SELF_UID).append("` INTEGER NOT NULL DEFAULT 0,")
			.append("`").append(UploadTask.KEY_NAME).append("` VARCHAR,")
			.append("`").append(UploadTask.KEY_PROGRESS).append("` INTEGER NOT NULL DEFAULT 0)");
		db.execSQL(sql.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		StringBuilder sql = new  StringBuilder()
			.append("DROP TABLE IF EXISTS `").append(ChatMessage.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(FriendMessage.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(Friend.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(CircleMessage.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(PrefixVideo.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(MvInfo.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(ActivityMessage.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(MultiChatMessage.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(HelpMessage.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(BookMessage.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(SimpleCircle.TABLE).append("`;")
			.append("DROP TABLE IF EXISTS `").append(UploadTask.TABLE).append("`;");
		db.execSQL(sql.toString());
		
		onCreate(db);
	}

}
