<template>
	<view class="m3-page">
		<!-- MD3 top app bar -->
		<view class="m3-appbar">
			<view class="m3-appbar-title"><text>今日假条</text></view>
			<view class="m3-appbar-action" @click="clearAll">清除</view>
		</view>

		<view class="m3-scroll">
			<!-- assist banner -->
			<view class="m3-banner" @click="tip">
				<view class="m3-banner-badge">?</view>
				<text class="m3-banner-text">为何不能请假？</text>
				<text class="m3-banner-arrow">›</text>
			</view>

			<!-- leave cards -->
			<view class="m3-card" v-for="(item,i) in leaves" :key="i" @click="clickContent(i)">
				<view class="m3-card-head">
					<text class="m3-card-title">我的 {{item.type}}申请</text>
					<text class="m3-card-date">{{item.startDate}}</text>
				</view>
				<view class="m3-card-line">
					<text class="m3-card-key">请假时间</text>
					<text class="m3-card-val">{{item.startDate}} 至 {{item.endDate}}</text>
				</view>
				<view class="m3-card-line" v-if="item.countDate">
					<text class="m3-card-key">时长</text>
					<text class="m3-card-val">{{item.countDate}}</text>
				</view>
				<view class="m3-card-foot">
					<view class="m3-chip" :class="chipClass(item.state)">
						<text class="m3-chip-dot"></text>
						<text>{{item.state}}</text>
					</view>
					<text class="m3-card-more">查看详情 →</text>
				</view>
			</view>

			<!-- empty state -->
			<view class="m3-empty" v-if="!leaves || leaves.length === 0">
				<view class="m3-empty-icon">🗒</view>
				<text class="m3-empty-title">还没有请假记录</text>
				<text class="m3-empty-sub">点击右下角「我要请假」开始</text>
			</view>

			<view class="m3-end" v-if="leaves && leaves.length > 0">没有更多了</view>
		</view>

		<!-- MD3 对话框 -->
		<view class="m3-scrim-dialog" v-if="dialog.show" @click="closeDialog"></view>
		<view class="m3-dialog" v-if="dialog.show">
			<view class="m3-dialog-icon">!</view>
			<text class="m3-dialog-title">清除全部记录？</text>
			<text class="m3-dialog-body">删除后无法恢复，确定要继续吗？</text>
			<view class="m3-dialog-actions">
				<text class="m3-dialog-btn" @click="closeDialog">取消</text>
				<text class="m3-dialog-btn is-primary" @click="confirmClear">清除</text>
			</view>
		</view>

		<!-- MD3 Snackbar（不拦截操作） -->
		<view class="m3-snackbar" v-if="snack.show">{{ snack.text }}</view>

		<!-- MD3 extended FAB -->
		<view class="m3-fab" @click="addLeave">
			<text class="m3-fab-plus">+</text>
			<text class="m3-fab-label">我要请假</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				leaves: null,
				loop: null,
				studentInfo: null,
				dialog: { show: false },
				snack: { show: false, text: '' }
			};
		},
		onShow() {
			var list = uni.getStorageSync('todaySchoolLeaves');
			if (!list || list.length === undefined) {
				list = [];
			}
			this.leaves = list.slice().reverse();
		},
		onBackPress(options) {
			if (options.from === 'navigateBack') {
				return false;
			}
			this.onreturn();
			return true;
		},
		methods: {
			onreturn() {
				uni.redirectTo({ url: './index' });
			},
			tip() {
				this.showSnack('请先填写请假信息后再提交');
			},

			showSnack(text) {
				var that = this;
				clearTimeout(this._snackTimer);
				this.snack = { show: true, text: text };
				this._snackTimer = setTimeout(function() {
					that.snack.show = false;
				}, 2200);
			},
			addLeave() {
				uni.navigateTo({ url: '../addLeave/addLeave' });
			},
			clearAll() {
				this.dialog.show = true;
			},

			closeDialog() {
				this.dialog.show = false;
			},

			confirmClear() {
				uni.setStorageSync('todaySchoolLeaves', null);
				this.leaves = [];
				this.dialog.show = false;
				this.showSnack('已清除全部记录');
			},
			chipClass(state) {
				if (state === '正在休假中') return 'is-active';
				if (state === '已过期') return 'is-error';
				return 'is-idle';
			},
			clickContent(i) {
				uni.setStorageSync('currLeaves', this.leaves[i]);
				uni.setStorageSync('currIndex', i);
				uni.navigateTo({
					url: '../viewLeaves/viewLeaves',
					animationType: 'none'
				});
			},
			touchStart() {
				var that = this;
				clearTimeout(this.loop);
				this.loop = setTimeout(function() {
					that.studentInfo = uni.getStorageSync('studentInfo');
					if (!that.studentInfo) {
						uni.showToast({ title: '扫码前需先设置个人信息', duration: 2000, icon: 'none' });
						uni.navigateTo({ url: '../addStudentInfo/addStudentInfo' });
					} else {
						uni.scanCode({
							success: function(e) {
								uni.navigateTo({ url: '../checkSuccess/checkSuccess' });
							}
						});
					}
				}, 700);
			},
			touchEnd() {
				clearTimeout(this.loop);
			}
		}
	};
</script>

<style>
	.m3-page {
		min-height: 100vh;
		background-color: var(--md-surface, #fafdf8);
		padding-top: var(--status-bar-height);
		padding-bottom: 96px;
		box-sizing: border-box;
		font-family: -apple-system, "HarmonyOS Sans SC", "PingFang SC", "Noto Sans CJK SC", sans-serif;
		color: var(--md-on-surface, #171d19);
	}

	/* ---------- top app bar ---------- */
	.m3-appbar {
		height: 64px;
		display: flex;
		align-items: center;
		padding: 0 8px;
		background-color: var(--md-surface, #fafdf8);
	}

	.m3-appbar-lead {
		width: 48px;
		height: 48px;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.m3-appbar-lead:active {
		background-color: var(--md-surface-container-high, #e6ebe4);
	}

	.m3-appbar-icon {
		width: 20px;
		height: 20px;
	}

	.m3-appbar-title {
		flex: 1;
		padding-left: 8px;
		font-size: 22px;
		line-height: 28px;
		font-weight: 600;
		letter-spacing: .2px;
		color: var(--md-on-surface, #171d19);
	}

	.m3-appbar-brand {
		width: 22px;
		height: 22px;
		margin-right: 8px;
	}

	.m3-appbar-action {
		min-width: 56px;
		height: 40px;
		padding: 0 12px;
		border-radius: 20px;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 14px;
		font-weight: 600;
		color: var(--md-primary, #166b4e);
	}

	.m3-appbar-action:active {
		background-color: var(--md-primary-container, #a6f2ce);
	}

	/* ---------- segmented ---------- */
	.m3-seg {
		margin: 2px 16px 8px;
		height: 40px;
		display: flex;
		align-items: center;
		padding: 3px;
		border-radius: 20px;
		background-color: var(--md-surface-container, #ecf1ea);
	}

	.m3-seg-item {
		flex: 1;
		height: 34px;
		line-height: 34px;
		text-align: center;
		font-size: 14px;
		font-weight: 600;
		color: var(--md-on-surface-variant, #404943);
		border-radius: 17px;
	}

	.m3-seg-item.is-on {
		background-color: var(--md-secondary-container, #cbe8da);
		color: var(--md-on-secondary-container, #04211a);
	}

	/* ---------- list ---------- */
	.m3-scroll {
		padding: 0 16px;
	}

	.m3-banner {
		display: flex;
		align-items: center;
		gap: 10px;
		height: 48px;
		padding: 0 14px;
		margin-bottom: 12px;
		border-radius: 16px;
		background-color: var(--md-tertiary-container, #ffdcc2);
	}

	.m3-banner:active {
		opacity: .85;
	}

	.m3-banner-badge {
		width: 22px;
		height: 22px;
		border-radius: 50%;
		background-color: var(--md-tertiary, #8b5000);
		color: #fff;
		font-size: 13px;
		font-weight: 700;
		text-align: center;
		line-height: 22px;
	}

	.m3-banner-text {
		flex: 1;
		font-size: 14px;
		font-weight: 600;
		color: var(--md-on-tertiary-container, #2e1500);
	}

	.m3-banner-arrow {
		font-size: 18px;
		color: var(--md-on-tertiary-container, #2e1500);
		opacity: .6;
	}

	.m3-card {
		padding: 18px 18px 14px;
		margin-bottom: 12px;
		border-radius: 28px;
		background-color: var(--md-surface-container-low, #f2f6f0);
		transition: transform .18s cubic-bezier(.2, 0, 0, 1), background-color .18s;
	}

	.m3-card:active {
		transform: scale(.985);
		background-color: var(--md-surface-container-high, #e6ebe4);
	}

	.m3-card-head {
		display: flex;
		align-items: baseline;
		justify-content: space-between;
		margin-bottom: 12px;
	}

	.m3-card-title {
		font-size: 19px;
		line-height: 24px;
		font-weight: 600;
		color: var(--md-on-surface, #171d19);
	}

	.m3-card-date {
		font-size: 12px;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-card-line {
		display: flex;
		margin-top: 6px;
		font-size: 13px;
		line-height: 18px;
	}

	.m3-card-key {
		width: 62px;
		flex: none;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-card-val {
		flex: 1;
		color: var(--md-on-surface, #171d19);
	}

	.m3-card-foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 14px;
	}

	.m3-chip {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		height: 30px;
		padding: 0 12px;
		border-radius: 8px;
		font-size: 13px;
		font-weight: 600;
	}

	.m3-chip-dot {
		width: 7px;
		height: 7px;
		border-radius: 50%;
		background-color: currentColor;
	}

	.m3-chip.is-active {
		background-color: var(--md-primary-container, #a6f2ce);
		color: var(--md-on-primary-container, #002116);
	}

	.m3-chip.is-idle {
		background-color: var(--md-surface-container-high, #e6ebe4);
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-chip.is-error {
		background-color: var(--md-error-container, #ffdad6);
		color: var(--md-on-error-container, #410002);
	}

	.m3-card-more {
		font-size: 13px;
		font-weight: 600;
		color: var(--md-primary, #166b4e);
	}

	.m3-empty {
		padding: 56px 24px;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.m3-empty-icon {
		font-size: 44px;
		line-height: 52px;
		margin-bottom: 8px;
	}

	.m3-empty-title {
		font-size: 17px;
		font-weight: 600;
		color: var(--md-on-surface, #171d19);
	}

	.m3-empty-sub {
		margin-top: 6px;
		font-size: 13px;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-end {
		text-align: center;
		font-size: 12px;
		color: var(--md-outline, #707972);
		padding: 12px 0 4px;
	}

	/* ---------- MD3 对话框 ---------- */
	.m3-scrim-dialog {
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, .32);
		z-index: 9000;
	}

	.m3-dialog {
		position: fixed;
		left: 24px;
		right: 24px;
		top: 50%;
		transform: translateY(-50%);
		z-index: 9001;
		padding: 24px 20px 12px;
		border-radius: 28px;
		background-color: var(--md-surface-container-high, #e6ebe4);
		display: flex;
		flex-direction: column;
		align-items: center;
		animation: m3-dialog-in .2s cubic-bezier(.2, 0, 0, 1);
	}

	@keyframes m3-dialog-in {
		from { opacity: 0; transform: translateY(-46%) scale(.96); }
		to { opacity: 1; transform: translateY(-50%) scale(1); }
	}

	.m3-dialog-icon {
		width: 44px;
		height: 44px;
		border-radius: 50%;
		background-color: var(--md-error-container, #ffdad6);
		color: var(--md-on-error-container, #410002);
		font-size: 22px;
		font-weight: 700;
		line-height: 44px;
		text-align: center;
		margin-bottom: 12px;
	}

	.m3-dialog-title {
		font-size: 18px;
		font-weight: 600;
		color: var(--md-on-surface, #171d19);
		text-align: center;
	}

	.m3-dialog-body {
		margin-top: 8px;
		font-size: 14px;
		line-height: 20px;
		color: var(--md-on-surface-variant, #404943);
		text-align: center;
	}

	.m3-dialog-actions {
		margin-top: 18px;
		display: flex;
		justify-content: flex-end;
		width: 100%;
		gap: 4px;
	}

	.m3-dialog-btn {
		min-width: 72px;
		height: 40px;
		line-height: 40px;
		text-align: center;
		border-radius: 20px;
		font-size: 14px;
		font-weight: 600;
		color: var(--md-on-surface-variant, #404943);
	}

	.m3-dialog-btn:active {
		background-color: var(--md-surface-container, #ecf1ea);
	}

	.m3-dialog-btn.is-primary {
		color: var(--md-primary, #166b4e);
	}

	/* ---------- MD3 Snackbar ---------- */
	.m3-snackbar {
		position: fixed;
		left: 16px;
		right: 16px;
		bottom: calc(88px + env(safe-area-inset-bottom));
		z-index: 9500;
		padding: 14px 16px;
		border-radius: 8px;
		background-color: #2E332E;
		color: #F0F4EF;
		font-size: 14px;
		line-height: 20px;
		box-shadow: 0 4px 12px rgba(0, 0, 0, .2);
		pointer-events: none;
		animation: m3-snack-in .2s cubic-bezier(.2, 0, 0, 1);
	}

	@keyframes m3-snack-in {
		from { opacity: 0; transform: translateY(12px); }
		to { opacity: 1; transform: translateY(0); }
	}

	/* ---------- extended FAB ---------- */
	.m3-fab {
		position: fixed;
		right: 16px;
		bottom: calc(20px + env(safe-area-inset-bottom));
		height: 56px;
		padding: 0 22px 0 16px;
		display: flex;
		align-items: center;
		gap: 10px;
		border-radius: 16px;
		background-color: var(--md-primary, #166b4e);
		color: var(--md-on-primary, #ffffff);
		box-shadow: 0 6px 16px rgba(0, 0, 0, .18);
		transition: transform .16s cubic-bezier(.2, 0, 0, 1);
	}

	.m3-fab:active {
		transform: scale(.96);
	}

	.m3-fab-plus {
		font-size: 24px;
		line-height: 24px;
		font-weight: 400;
	}

	.m3-fab-label {
		font-size: 15px;
		font-weight: 600;
		letter-spacing: .3px;
	}
</style>
