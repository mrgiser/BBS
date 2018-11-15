package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.OptionUtil;
import cn.he.zhao.bbs.entityUtil.PermissionUtil;
import cn.he.zhao.bbs.entityUtil.RoleUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.spring.MD5;
import cn.he.zhao.bbs.util.Ids;
import cn.he.zhao.bbs.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Initialization management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.1.11, Jun 25, 2018
 * @since 1.8.0
 */
@Service
public class InitMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InitMgmtService.class);

    /**
     * Default language.
     */
    private static final String DEFAULT_LANG = "zh_CN";

    private static Set<String> VISITOR_PERMISSIONS = new HashSet<>();
    private static Set<String> DEFAULT_PERMISSIONS = new HashSet<>();
    private static Set<String> MEMBER_PERMISSIONS = new HashSet<>();
    private static Set<String> REGULAR_PERMISSIONS = new HashSet<>();
    private static Set<String> ADMIN_PERMISSIONS = new HashSet<>();
    private static Set<String> LEADER_PERMISSIONS = new HashSet<>();

    static { // Init built-in roles' permissions, see https://github.com/b3log/symphony/issues/358 for more details
        // Visitor
        // no permissions at present

        // Default
        DEFAULT_PERMISSIONS.addAll(VISITOR_PERMISSIONS);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_ADD_ARTICLE);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_UPDATE_ARTICLE);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_REMOVE_ARTICLE);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_ADD_COMMENT);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_ADD_BREEZEMOON);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_UPDATE_BREEZEMOON);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_REMOVE_BREEZEMOON);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_UPDATE_COMMENT);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_REMOVE_COMMENT);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_THANK_ARTICLE);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_THANK_COMMENT);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_WATCH_ARTICLE);
        DEFAULT_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_FOLLOW_ARTICLE);

        // Member
        MEMBER_PERMISSIONS.addAll(DEFAULT_PERMISSIONS);
        MEMBER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_GOOD_ARTICLE);
        MEMBER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_BAD_ARTICLE);
        MEMBER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_GOOD_COMMENT);
        MEMBER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_BAD_COMMENT);
        MEMBER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_AT_USER);
        MEMBER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);

        // Regular
        REGULAR_PERMISSIONS.addAll(MEMBER_PERMISSIONS);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_STICK_ARTICLE);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_ADD_ARTICLE_ANONYMOUS);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_ADD_COMMENT_ANONYMOUS);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_VIEW_ARTICLE_HISTORY);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_VIEW_COMMENT_HISTORY);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_AT_PARTICIPANTS);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMON_EXCHANGE_INVITATION_CODE);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_TAG_UPDATE_TAG_BASIC);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN);
        REGULAR_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_TAGS);

        // Leader
        LEADER_PERMISSIONS.addAll(REGULAR_PERMISSIONS);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_USER_ADD_USER);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_USER_UPDATE_USER_BASIC);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_USER_UPDATE_USER_ADVANCED);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_ARTICLE_UPDATE_ARTICLE_BASIC);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_ARTICLE_STICK_ARTICLE);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_ARTICLE_CANCEL_STICK_ARTICLE);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_ARTICLE_REINDEX_ARTICLE_INDEX);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMENT_UPDATE_COMMENT_BASIC);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_BREEZEMOON_UPDATE_BREEZEMOON);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_BREEZEMOON_REMOVE_BREEZEMOON);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_USER_ADD_POINT);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_USER_EXCHANGE_POINT);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_USER_DEDUCT_POINT);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_IC_GEN_IC);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_IC_UPDATE_IC_BASIC);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_RW_ADD_RW);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_RW_UPDATE_RW_BASIC);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_RW_REMOVE_RW);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_USERS);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_BREEZEMOONS);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_ARTICLES);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_COMMENTS);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_ICS);
        LEADER_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_RWS);

        // Admin
        ADMIN_PERMISSIONS.addAll(LEADER_PERMISSIONS);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_ARTICLE_ADD_ARTICLE);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_ARTICLE_REINDEX_ARTICLES_INDEX);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_ARTICLE_REMOVE_ARTICLE);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_COMMENT_REMOVE_COMMENT);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN_TAG);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN_TAG);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_DOMAIN_UPDATE_DOMAIN_BASIC);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_AD_UPDATE_SIDE);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_AD_UPDATE_BANNER);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MISC_ALLOW_ADD_ARTICLE);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MISC_ALLOW_ADD_COMMENT);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MISC_LANGUAGE);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MISC_REGISTER_METHOD);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_DOMAINS);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_AD);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_ROLES);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_MISC);
        ADMIN_PERMISSIONS.add(PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_REPORTS);
    }

    /**
     * PermissionUtil Mapper.
     */
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * RoleUtil Mapper.
     */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * RoleUtil-permission Mapper.
     */
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    /**
     * OptionUtil Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Tag management service.
     */
    @Autowired
    private TagMgmtService tagMgmtService;

    /**
     * Article management service.
     */
    @Autowired
    private ArticleMgmtService articleMgmtService;

    /**
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Initializes Sym if first time setup.
     */
    public void initSym() {
        try {
            final List<UserExt> admins = userQueryService.getAdmins();

            if (null != admins && !admins.isEmpty()) { // Initialized already
                return;
            }
        } catch ( final Exception e) {
            LOGGER.error( "Check init error", e);

            System.exit(0);
        }

        LOGGER.info("It's your first time setup Sym, initializes Sym....");

        try {
//            LOGGER.info( "Database [{0}], creating all tables", Latkes.getRuntimeDatabase());

            // TODO: 2018/9/30 初始化创建数据库表
//            final List<JdbcRepositories.CreateTableResult> createTableResults = JdbcRepositories.initAllTables();
//            for (final JdbcRepositories.CreateTableResult createTableResult : createTableResults) {
//                LOGGER.info( "Creates table result [tableName={0}, isSuccess={1}]",
//                        createTableResult.getName(), createTableResult.isSuccess());
//            }

//            final Transaction transaction = optionMapper.beginTransaction();

            // Init statistic
            Option option = new Option();
            option.setOid(OptionUtil.ID_C_STATISTIC_MEMBER_COUNT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_STATISTIC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_STATISTIC_CMT_COUNT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_STATISTIC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_STATISTIC_ARTICLE_COUNT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_STATISTIC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_STATISTIC_DOMAIN_COUNT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_STATISTIC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_STATISTIC_TAG_COUNT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_STATISTIC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_STATISTIC_LINK_COUNT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_STATISTIC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_STATISTIC_MAX_ONLINE_VISITOR_COUNT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_STATISTIC);
            optionMapper.add(option);

            // Init misc
            option = new Option();
            option.setOid( OptionUtil.ID_C_MISC_ALLOW_REGISTER);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_MISC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_MISC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_MISC_ALLOW_ADD_ARTICLE);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_MISC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_MISC_ALLOW_ADD_COMMENT);
            option.setOptionValue( "0");
            option.setOptionCategory( OptionUtil.CATEGORY_C_MISC);
            optionMapper.add(option);

            option = new Option();
            option.setOid( OptionUtil.ID_C_MISC_LANGUAGE);
            option.setOptionValue( DEFAULT_LANG);
            option.setOptionCategory( OptionUtil.CATEGORY_C_MISC);
            optionMapper.add(option);

            // Init permissions
            final Permission permission = new Permission();

            // ad management permissions
            permission.setPermissionCategory(PermissionUtil.PERMISSION_CATEGORY_C_AD);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_AD_UPDATE_SIDE);
            permissionMapper.add(permission);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_AD_UPDATE_BANNER);
            permissionMapper.add(permission);

            // article management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_ARTICLE);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_ARTICLE_ADD_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_ARTICLE_CANCEL_STICK_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_ARTICLE_REINDEX_ARTICLES_INDEX);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_ARTICLE_REINDEX_ARTICLE_INDEX);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_ARTICLE_REMOVE_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_ARTICLE_STICK_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_ARTICLE_UPDATE_ARTICLE_BASIC);
            permissionMapper.add(permission);

            // comment management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_COMMENT);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMENT_REMOVE_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMENT_UPDATE_COMMENT_BASIC);
            permissionMapper.add(permission);

            // breezemoon management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_BREEZEMOON);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_BREEZEMOON_REMOVE_BREEZEMOON);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_BREEZEMOON_UPDATE_BREEZEMOON);
            permissionMapper.add(permission);

            // common permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_COMMON);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_ADD_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_ADD_ARTICLE_ANONYMOUS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_UPDATE_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_REMOVE_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_ADD_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_ADD_BREEZEMOON);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_UPDATE_BREEZEMOON);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_REMOVE_BREEZEMOON);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_ADD_COMMENT_ANONYMOUS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_UPDATE_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_REMOVE_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_VIEW_COMMENT_HISTORY);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_AT_USER);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_AT_PARTICIPANTS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_BAD_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_BAD_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_EXCHANGE_INVITATION_CODE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_FOLLOW_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_WATCH_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_GOOD_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_GOOD_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_STICK_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_THANK_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_THANK_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_USE_INVITATION_LINK);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_COMMON_VIEW_ARTICLE_HISTORY);
            permissionMapper.add(permission);

            // domain management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_DOMAIN);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_DOMAIN_ADD_DOMAIN_TAG);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_DOMAIN_REMOVE_DOMAIN_TAG);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_DOMAIN_UPDATE_DOMAIN_BASIC);
            permissionMapper.add(permission);

            // invitecode management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_IC);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_IC_GEN_IC);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_IC_UPDATE_IC_BASIC);
            permissionMapper.add(permission);

            // misc management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_MISC);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_MISC_ALLOW_ADD_ARTICLE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MISC_ALLOW_ADD_COMMENT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MISC_ALLOW_ANONYMOUS_VIEW);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MISC_LANGUAGE);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MISC_REGISTER_METHOD);
            permissionMapper.add(permission);

            // reserved word management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_RESERVED_WORD);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_RW_ADD_RW);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_RW_REMOVE_RW);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_RW_UPDATE_RW_BASIC);
            permissionMapper.add(permission);

            // tag management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_TAG);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_TAG_UPDATE_TAG_BASIC);
            permissionMapper.add(permission);

            // user management permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_USER);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_USER_ADD_POINT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_USER_ADD_USER);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_USER_DEDUCT_POINT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_USER_EXCHANGE_POINT);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_USER_UPDATE_USER_ADVANCED);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_USER_UPDATE_USER_BASIC);
            permissionMapper.add(permission);

            // menu permissions
            permission.setPermissionCategory( PermissionUtil.PERMISSION_CATEGORY_C_MENU);

            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_AD);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_ARTICLES);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_COMMENTS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_DOMAINS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_ICS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_RWS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_TAGS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_USERS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_BREEZEMOONS);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_MISC);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_ROLES);
            permissionMapper.add(permission);
            permission.setOid( PermissionUtil.PERMISSION_ID_C_MENU_ADMIN_REPORTS);
            permissionMapper.add(permission);

            // Init roles
            final Role role = new Role();

            role.setOid( RoleUtil.ROLE_ID_C_ADMIN);
            role.setRoleName( "Admin");
            role.setRoleDescription( "");
            roleMapper.add(role);

            role.setOid( RoleUtil.ROLE_ID_C_DEFAULT);
            role.setRoleName( "Default");
            role.setRoleDescription( "");
            roleMapper.add(role);

            role.setOid( RoleUtil.ROLE_ID_C_LEADER);
            role.setRoleName( "Leader");
            role.setRoleDescription( "");
            roleMapper.add(role);

            role.setOid( RoleUtil.ROLE_ID_C_MEMBER);
            role.setRoleName( "Member");
            role.setRoleDescription( "");
            roleMapper.add(role);

            role.setOid( RoleUtil.ROLE_ID_C_REGULAR);
            role.setRoleName( "Regular");
            role.setRoleDescription( "");
            roleMapper.add(role);

            role.setOid( RoleUtil.ROLE_ID_C_VISITOR);
            role.setRoleName( "Visitor");
            role.setRoleDescription( "");
            roleMapper.add(role);

            // Init RoleUtil-PermissionUtil
            final RolePermission rolePermission = new RolePermission();

            // [Default] role's permissions
            rolePermission.setRoleId( RoleUtil.ROLE_ID_C_DEFAULT);
            for (final String permissionId : DEFAULT_PERMISSIONS) {
                rolePermission.setOid( Ids.genTimeMillisId());
                rolePermission.setPermissionId(permissionId);

                rolePermissionMapper.add(rolePermission);
            }

            // [Member] role's permissions
            rolePermission.setRoleId( RoleUtil.ROLE_ID_C_MEMBER);
            for (final String permissionId : MEMBER_PERMISSIONS) {
                rolePermission.setOid(Ids.genTimeMillisId());
                rolePermission.setPermissionId(permissionId);

                rolePermissionMapper.add(rolePermission);
            }

            // [Regular] role's permissions
            rolePermission.setRoleId( RoleUtil.ROLE_ID_C_REGULAR);
            for (final String permissionId : REGULAR_PERMISSIONS) {
                rolePermission.setOid( Ids.genTimeMillisId());
                rolePermission.setPermissionId(permissionId);

                rolePermissionMapper.add(rolePermission);
            }

            // [Leader] role's permissions
            rolePermission.setRoleId( RoleUtil.ROLE_ID_C_LEADER);
            for (final String permissionId : LEADER_PERMISSIONS) {
                rolePermission.setOid(Ids.genTimeMillisId());
                rolePermission.setPermissionId(permissionId);

                rolePermissionMapper.add(rolePermission);
            }

            // [Admin] role's permissions
            rolePermission.setRoleId( RoleUtil.ROLE_ID_C_ADMIN);
            for (final String permissionId : ADMIN_PERMISSIONS) {
                rolePermission.setOid(Ids.genTimeMillisId());
                rolePermission.setPermissionId(permissionId);

                rolePermissionMapper.add(rolePermission);
            }

//            transaction.commit();

            // Init admin
            final UserExt admin = new UserExt();
            admin.setUserEmail("admin" + UserExtUtil.USER_BUILTIN_EMAIL_SUFFIX);
            admin.setUserName( "admin");
            admin.setUserPassword( MD5.hash("admin"));
            admin.setUserLanguage( DEFAULT_LANG);
            admin.setUserRole(RoleUtil.ROLE_ID_C_ADMIN);
            admin.setUserStatus( UserExtUtil.USER_STATUS_C_VALID);
            admin.setUserGuideStep(UserExtUtil.USER_GUIDE_STEP_FIN);
            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(admin));
            final String adminId = userMgmtService.addUser(jsonObject);
            admin.setOid(adminId);

            // Init default commenter (for sync comment from client)
            final UserExt defaultCommenter = new UserExt();
            defaultCommenter.setUserEmail( UserExtUtil.DEFAULT_CMTER_EMAIL);
            defaultCommenter.setUserName(UserExtUtil.DEFAULT_CMTER_NAME);
            defaultCommenter.setUserPassword(MD5.hash(String.valueOf(new Random().nextInt())));
            defaultCommenter.setUserLanguage("en_US");
            defaultCommenter.setUserGuideStep(UserExtUtil.USER_GUIDE_STEP_FIN);
            defaultCommenter.setUserRole(UserExtUtil.DEFAULT_CMTER_ROLE);
            defaultCommenter.setUserStatus( UserExtUtil.USER_STATUS_C_VALID);

            JSONObject jsonObject_defaultCommenter = new JSONObject(JsonUtil.objectToJson(defaultCommenter));
            userMgmtService.addUser(jsonObject_defaultCommenter);

            // Add tags
            String tagTitle = Symphonys.get("systemAnnounce");
            String tagId = tagMgmtService.addTag(adminId, tagTitle);
            Tag tag = tagMapper.get(tagId);
            tag.setTagURI( "announcement");
            tagMgmtService.updateTag(tagId, tag);

            tagTitle = "B3log";
            tagId = tagMgmtService.addTag(adminId, tagTitle);
            tag = tagMapper.get(tagId);
            tag.setTagURI( "B3log");
            tag.setTagIconPath( "b3log.png");
            tag.setTagDescription("[B3log](https://b3log.org) 是一个开源组织，名字来源于“Bulletin Board Blog”缩写，目标是将独立博客与论坛结合，形成一种新的网络社区体验，详细请看 [B3log 构思](https://hacpai.com/b3log)。目前 B3log 已经开源了多款产品： [Solo] 、 [Sym] 、 [Wide] 。");
            tagMgmtService.updateTag(tagId, tag);

            tagTitle = "Sym";
            tagId = tagMgmtService.addTag(adminId, tagTitle);
            tag = tagMapper.get(tagId);
            tag.setTagURI( "Sym");
            tag.setTagIconPath( "sym.png");
            tag.setTagDescription( "[Sym](https://github.com/b3log/symphony) 是一个用 [Java] 实现的现代化社区（论坛/社交网络/博客）平台，“下一代的社区系统，为未来而构建”。");
            tagMgmtService.updateTag(tagId, tag);

            tagTitle = "Solo";
            tagId = tagMgmtService.addTag(adminId, tagTitle);
            tag = tagMapper.get(tagId);
            tag.setTagURI( "Solo");
            tag.setTagIconPath( "solo.png");
            tag.setTagDescription( "[Solo](https://github.com/b3log/solo) 是目前 GitHub 上关注度最高的 Java 开源博客系统。\n" +
                    "\n" +
                    "* [项目地址](https://github.com/b3log/solo)\n" +
                    "* [用户指南](https://hacpai.com/article/1492881378588)");
            tagMgmtService.updateTag(tagId, tag);

            tagTitle = "Pipe";
            tagId = tagMgmtService.addTag(adminId, tagTitle);
            tag = tagMapper.get(tagId);
            tag.setTagURI( "Pipe");
            tag.setTagIconPath( "pipe.png");
            tag.setTagDescription( "[Pipe](https://github.com/b3log/pipe) 是一款小而美的开源博客平台，通过 [黑客派] 账号登录即可使用。如果你不想自己搭建，可以直接使用我们运维的 http://pipe.b3log.org");
            tagMgmtService.updateTag(tagId, tag);

            tagTitle = "Wide";
            tagId = tagMgmtService.addTag(adminId, tagTitle);
            tag = tagMapper.get(tagId);
            tag.setTagURI( "Wide");
            tag.setTagIconPath( "wide.png");
            tag.setTagDescription( "[Wide](https://github.com/b3log/wide) 是一个基于 [Web] 的 <a href='/tags/golang'>Go</a> 语言团队 IDE。通过浏览器就可以进行 Go 开发，并有代码自动完成、查看表达式、编译反馈、Lint、实时结果输出等功能。");
            tagMgmtService.updateTag(tagId, tag);

            // Hello World!
            final Article article = new Article();
            article.setArticleTitle( "Welcome to Sym community :gift_heart:");
            article.setArticleTags("Sym,Announcement");
            article.setArticleContent( "Hello, everyone!");
            article.setArticleType( 0);
            article.setArticleAuthorId(admin.getOid());

            articleMgmtService.addArticle(article);

            LOGGER.info("Initialized Sym, have fun :)");
        } catch (final Exception e) {
            LOGGER.error( "Initializes Sym failed", e);

            System.exit(0);
        }
    }
}
